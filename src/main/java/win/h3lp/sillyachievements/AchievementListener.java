package win.h3lp.sillyachievements;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class AchievementListener implements Listener {

    private final Sillyachievements plugin;
    private final HashMap<UUID, Long> chickenChaseStartTime;
    private final HashMap<UUID, Integer> jumpCount;
    private final HashMap<UUID, Integer> placedAndBrokenCount;

    public AchievementListener(Sillyachievements plugin) {
        this.plugin = plugin;
        this.chickenChaseStartTime = new HashMap<>();
        this.jumpCount = new HashMap<>();
        this.placedAndBrokenCount = new HashMap<>();
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

    @EventHandler
    public void onPlayerJump(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            int jumps = jumpCount.getOrDefault(player.getUniqueId(), 0) + 1;
            jumpCount.put(player.getUniqueId(), jumps);
            if (jumps >= 500) {
                awardAchievement(player, "Jumping Jack");
                jumpCount.put(player.getUniqueId(), 0);
            }
        }
    }

    @EventHandler
    public void onPlayerRidePig(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isInsideVehicle() && player.getVehicle().getType() == EntityType.PIG) {
            double distance = player.getVehicle().getVelocity().length();
            FileConfiguration config = plugin.getConfig();
            double pigDistance = config.getDouble(player.getUniqueId() + ".pigDistance", 0) + distance;
            config.set(player.getUniqueId() + ".pigDistance", pigDistance);
            if (pigDistance >= 1000) {
                awardAchievement(player, "Pig Rider");
                config.set(player.getUniqueId() + ".pigDistance", 0);
            }
            plugin.saveConfig();
        }
    }

    @EventHandler
    public void onPlayerHoldItem(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItem(event.getNewSlot()) != null && player.getInventory().getItem(event.getNewSlot()).getType() == Material.DIAMOND) {
            awardAchievement(player, "Selfie Time");
        }
    }

    @EventHandler
    public void onBlockPlace(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            FileConfiguration config = plugin.getConfig();
            String key = player.getUniqueId() + ".placedAndBroken";
            int count = config.getInt(key, 0) + 1;
            config.set(key, count);
            if (count >= 50) {
                awardAchievement(player, "Failed Architect");
                config.set(key, 0);
            }
            plugin.saveConfig();
        }
    }

    @EventHandler
    public void onBlockBreak(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            FileConfiguration config = plugin.getConfig();
            String key = player.getUniqueId() + ".placedAndBroken";
            int count = config.getInt(key, 0) + 1;
            config.set(key, count);
            if (count >= 50) {
                awardAchievement(player, "Failed Architect");
                config.set(key, 0);
            }
            plugin.saveConfig();
        }
    }

    @EventHandler
    public void onPlayerCollectDirt(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.DIRT) {
            FileConfiguration config = plugin.getConfig();
            int dirtCollected = config.getInt(player.getUniqueId() + ".dirtCollected", 0) + 1;
            config.set(player.getUniqueId() + ".dirtCollected", dirtCollected);
            if (dirtCollected >= 1000) {
                awardAchievement(player, "Dirt Collector");
                config.set(player.getUniqueId() + ".dirtCollected", 0);
            }
            plugin.saveConfig();
        }
    }

    @EventHandler
    public void onPlayerFallFromHeight(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                if (event.getDamage() >= 100) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (player.isOnline() && !player.isDead()) {
                                awardAchievement(player, "Sky Diver");
                            }
                        }
                    }.runTaskLater(plugin, 100); // 5 seconds
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRun(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        if (player.isSprinting()) {
            double distance = player.getVelocity().length();
            FileConfiguration config = plugin.getConfig();
            double runDistance = config.getDouble(player.getUniqueId() + ".runDistance", 0) + distance;
            config.set(player.getUniqueId() + ".runDistance", runDistance);
            if (runDistance >= 10000) {
                awardAchievement(player, "Marathon Runner");
                config.set(player.getUniqueId() + ".runDistance", 0);
            }
            plugin.saveConfig();
        }
    }

    @EventHandler
    public void onPlayerCatchFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            FileConfiguration config = plugin.getConfig();
            int fishCaught = config.getInt(player.getUniqueId() + ".fishCaught", 0) + 1;
            config.set(player.getUniqueId() + ".fishCaught", fishCaught);
            if (fishCaught >= 100) {
                awardAchievement(player, "Fishy Business");
                config.set(player.getUniqueId() + ".fishCaught", 0);
            }
            plugin.saveConfig();
        }
    }

    @EventHandler
    public void onPlayerCookItem(FurnaceExtractEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();
        int itemsCooked = config.getInt(player.getUniqueId() + ".itemsCooked", 0) + event.getItemAmount();
        config.set(player.getUniqueId() + ".itemsCooked", itemsCooked);
        if (itemsCooked >= 100) {
            awardAchievement(player, "Master Chef");
            config.set(player.getUniqueId() + ".itemsCooked", 0);
        }
        plugin.saveConfig();
    }

    @EventHandler
    public void onPlayerKillPhantom(EntityDeathEvent event) {
        if (event.getEntity().getType() == EntityType.PHANTOM && event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            FileConfiguration config = plugin.getConfig();
            int phantomsKilled = config.getInt(player.getUniqueId() + ".phantomsKilled", 0) + 1;
            config.set(player.getUniqueId() + ".phantomsKilled", phantomsKilled);
            if (phantomsKilled >= 50) {
                awardAchievement(player, "Ghost Buster");
                config.set(player.getUniqueId() + ".phantomsKilled", 0);
            }
            plugin.saveConfig();
        }
    }

    private void awardAchievement(Player player, String achievement) {
        player.sendMessage(ChatColor.GOLD + "Achievement Unlocked: " + ChatColor.GREEN + achievement);
        Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + " has unlocked the achievement: " + achievement);
    }
}
