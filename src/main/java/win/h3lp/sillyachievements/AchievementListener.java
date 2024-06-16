package win.h3lp.sillyachievements;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class AchievementListener implements Listener {

    private final Sillyachievements plugin;
    private final HashMap<UUID, Long> chickenChaseStartTime;

    public AchievementListener(Sillyachievements plugin) {
        this.plugin = plugin;
        this.chickenChaseStartTime = new HashMap<>();
    }

    @EventHandler
    public void onPlayerPunchTree(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.OAK_LOG) {
            FileConfiguration config = plugin.getConfig();
            int punches = config.getInt(player.getUniqueId() + ".treePunches", 0) + 1;
            config.set(player.getUniqueId() + ".treePunches", punches);
            if (punches >= 100) {
                awardAchievement(player, "Tree Hugger");
                config.set(player.getUniqueId() + ".treePunches", 0);
            }
            plugin.saveConfig();
        }
    }

    @EventHandler
    public void onPlayerChaseChicken(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        boolean nearChicken = player.getNearbyEntities(5, 5, 5).stream().anyMatch(e -> e.getType() == EntityType.CHICKEN);

        if (nearChicken) {
            if (!chickenChaseStartTime.containsKey(player.getUniqueId())) {
                chickenChaseStartTime.put(player.getUniqueId(), System.currentTimeMillis());
            } else {
                long chaseDuration = System.currentTimeMillis() - chickenChaseStartTime.get(player.getUniqueId());
                if (chaseDuration >= 30000) { // 30 seconds
                    awardAchievement(player, "Chicken Chaser");
                    chickenChaseStartTime.remove(player.getUniqueId());
                }
            }
        } else {
            chickenChaseStartTime.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerFallInLava(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline() && !player.isDead()) {
                            awardAchievement(player, "Lava Lover");
                        }
                    }
                }.runTaskLater(plugin, 100); // 5 seconds
            }
        }
    }

    private void awardAchievement(Player player, String achievement) {
        player.sendMessage(ChatColor.GOLD + "Achievement Unlocked: " + ChatColor.GREEN + achievement);
        Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + " has unlocked the achievement: " + achievement);
    }
}