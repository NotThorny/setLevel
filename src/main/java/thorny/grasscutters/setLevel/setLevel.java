package thorny.grasscutters.setLevel;

import emu.grasscutter.plugin.Plugin;

public final class setLevel extends Plugin {
    private static setLevel instance;
    public static setLevel getInstance() {
        return instance;
    }
    @Override public void onLoad() {
        // Set the plugin instance.
        instance = this;
    }
    @Override public void onEnable() {

        // Register commands.
        this.getHandle().registerCommand(new thorny.grasscutters.setLevel.commands.setLevelCommand());

        // Log a plugin status message.
        this.getLogger().info("The setLevel plugin has been enabled.");
    }

    @Override public void onDisable() {
        // Log a plugin status message.
        this.getLogger().info("How could you do this to me... setLevel has been disabled.");
    }
}