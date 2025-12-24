# Alt Panel Overlay Plugin

This live-plugin shows icons for IntelliJ tool windows when you hold down the Alt key. You can hover over these icons while continuing to hold Alt to close the corresponding tool window.

## Features

- **Hold Alt**: Shows icons for available tool windows (panels) positioned around your mouse cursor
- **Hover to Close**: While holding Alt, hover over an icon to close that tool window
- **Release Alt**: Hides the overlay

## How to Use

1. **Load the plugin**: Run this plugin from the LivePlugin tool window
2. **Hold Alt key**: Icons for tool windows (like Project, Terminal, Gradle, etc.) will appear near your mouse cursor
3. **Hover over an icon**: While still holding Alt, move your mouse over an icon to close that panel
4. **Release Alt**: The overlay disappears

## Icon Positions

Icons are positioned around the mouse cursor:
- **Below** the cursor
- **Left** of the cursor
- **Right** of the cursor

Up to 3 tool windows are displayed at a time.

## Technical Details

This plugin:
- Registers a global keyboard event listener for the Alt key
- Uses IntelliJ's `ToolWindowManager` to enumerate available tool windows
- Creates a transparent overlay panel on the IDE's glass pane
- Displays tool window icons with mouse hover detection
- Automatically cleans up resources when the plugin is unloaded
