package win.h3lp.sillyachievements;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Sillyachievements extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(new AchievementListener(this), this);
        getLogger().info("SillyAchievements has been enabled!");

    }

    @Override
    public void onDisable() {
        getLogger().info("SillyAchievements has been disabled.");
    }
}
