Minimal Clock Widget – dark settings screen fix

Replace these two files in your GitHub repository:
1. app/src/main/java/com/example/minimalclock/SettingsActivity.kt
2. app/src/main/res/values/styles.xml

Keep all other project files unchanged. Commit the changes to the main branch.
Then open GitHub Actions and run the existing Build APK workflow.
Install the newly built APK and test the settings screen.

This changes the settings screen to a full-height dark theme, improves spacing,
makes slider values visible, and gives the spinner controls a dark theme.
It preserves the existing preference keys and widget update calls.
