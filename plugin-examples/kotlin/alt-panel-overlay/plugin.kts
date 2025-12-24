import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerEx
import liveplugin.*
import java.awt.*
import java.awt.event.*
import javax.swing.*

// Plugin that shows icons for tool windows when Alt key is held down
// Hovering over an icon while Alt is held will close that tool window

var overlayPanel: JPanel? = null
var currentProject: Project? = null
var keyEventDispatcher: KeyEventDispatcher? = null

fun createOverlayPanel(): JPanel = JPanel(null).apply {
    isOpaque = false
    isVisible = false
    background = Color(0, 0, 0, 0)
}

fun setupOverlay(project: Project) {
    if (currentProject == project && overlayPanel != null) return
    currentProject = project
    
    val frame = WindowManager.getInstance().getFrame(project) ?: return
    val rootPane = frame.rootPane ?: return
    
    // Create new overlay panel
    overlayPanel = createOverlayPanel()
    
    // Use glass pane to draw overlay without blocking other components
    val glassPane = rootPane.glassPane as? JComponent ?: return
    glassPane.layout = null
    glassPane.add(overlayPanel)
    glassPane.isVisible = true
    
    overlayPanel?.setBounds(0, 0, rootPane.width, rootPane.height)
}

fun showPanelIcons(project: Project, mousePoint: Point) {
    val panel = overlayPanel ?: return
    panel.removeAll()
    panel.isVisible = true
    
    val toolWindowManager = ToolWindowManagerEx.getInstanceEx(project)
    val toolWindowIds = toolWindowManager.toolWindowIds
    
    // Position icons around the mouse cursor
    val iconSize = 48
    val spacing = 10
    val positions = listOf(
        Point(0, iconSize + spacing),        // Below
        Point(-(iconSize + spacing), 0),     // Left
        Point(iconSize + spacing, 0)         // Right
    )
    
    var posIndex = 0
    for (toolWindowId in toolWindowIds) {
        val toolWindow = toolWindowManager.getToolWindow(toolWindowId) ?: continue
        if (!toolWindow.isAvailable) continue
        
        val icon = toolWindow.icon ?: continue
        if (posIndex >= positions.size) break
        
        val position = positions[posIndex]
        val iconLabel = JLabel(icon).apply {
            val x = mousePoint.x + position.x
            val y = mousePoint.y + position.y
            setBounds(x, y, iconSize, iconSize)
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            toolTipText = toolWindow.stripeTitle
            
            // Add mouse listener to close panel on hover
            addMouseListener(object : MouseAdapter() {
                override fun mouseEntered(e: MouseEvent) {
                    // Check if Alt is still held
                    if ((e.modifiersEx and InputEvent.ALT_DOWN_MASK) != 0) {
                        if (toolWindow.isVisible) {
                            toolWindow.hide()
                        }
                    }
                }
            })
        }
        
        panel.add(iconLabel)
        posIndex++
    }
    
    panel.revalidate()
    panel.repaint()
}

fun hideOverlay() {
    overlayPanel?.isVisible = false
    overlayPanel?.removeAll()
}

fun cleanup() {
    hideOverlay()
    keyEventDispatcher?.let {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(it)
    }
    keyEventDispatcher = null
    overlayPanel = null
    currentProject = null
}

// Register global key listener for Alt key
registerProjectOpenListener(pluginDisposable) { project ->
    setupOverlay(project)
    
    keyEventDispatcher = KeyEventDispatcher { e ->
        when (e.id) {
            KeyEvent.KEY_PRESSED -> {
                if (e.keyCode == KeyEvent.VK_ALT) {
                    val panel = overlayPanel
                    if (panel != null && !panel.isVisible) {
                        val mousePosition = MouseInfo.getPointerInfo()?.location
                        if (mousePosition != null) {
                            val frame = WindowManager.getInstance().getFrame(project)
                            if (frame != null) {
                                try {
                                    // Convert screen coordinates to component coordinates
                                    val frameLocation = frame.locationOnScreen
                                    val relativePoint = Point(
                                        mousePosition.x - frameLocation.x,
                                        mousePosition.y - frameLocation.y
                                    )
                                    showPanelIcons(project, relativePoint)
                                } catch (e: Exception) {
                                    // Ignore exceptions from getting screen location
                                }
                            }
                        }
                    }
                }
            }
            KeyEvent.KEY_RELEASED -> {
                if (e.keyCode == KeyEvent.VK_ALT) {
                    hideOverlay()
                }
            }
        }
        false // Don't consume the event
    }
    
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher)
    
    pluginDisposable.whenDisposed {
        cleanup()
    }
}

if (!isIdeStartup) {
    show("Loaded Alt Panel Overlay Plugin<br/>Hold Alt to see panel icons near mouse cursor<br/>Hover over icons while holding Alt to close panels")
}
