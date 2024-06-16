package win.h3lp.sillyachievements;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AchievementListener implements Listener {

    private final Sillyachievements plugin;
    private final Map<UUID, Long> chickenChaseStartTime = new HashMap<>();
    private final Map<UUID, Integer> jumpCount = new HashMap<>();
    private final Map<UUID, Integer> placedAndBrokenCount = new HashMap<>();
    private final Map<UUID, Set<Biome>> visitedBiomes = new HashMap<>();
    private final Map<UUID, Long> awakeStartTime = new HashMap<>();
    private final Map<UUID, Long> stargazerStartTime = new HashMap<>();

    public AchievementListener(Sillyachievements plugin) {
        this.plugin = plugin;
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
                config.set(player.getUniqueId() + ".treePunches", 0); // Reset count or set it to a higher threshold for repeated achievements
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
                }.runTaskLater(plugin, 100); // Check after 5 seconds (100 ticks)
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
                jumpCount.put(player.getUniqueId(), 0); // Reset count or set it to a higher threshold for repeated achievements
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
                config.set(player.getUniqueId() + ".pigDistance", 0); // Reset count or set it to a higher threshold for repeated achievements
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
                config.set(player.getUniqueId() + ".dirtCollected", 0); // Reset count or set it to a higher threshold for repeated achievements
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
                    }.runTaskLater(plugin, 100); // Check after 5 seconds (100 ticks)
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
                config.set(player.getUniqueId() + ".runDistance", 0); // Reset count or set it to a higher threshold for repeated achievements
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
                config.set(player.getUniqueId() + ".fishCaught", 0); // Reset count or set it to a higher threshold for repeated achievements
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
            config.set(player.getUniqueId() + ".itemsCooked", 0); // Reset count or set it to a higher threshold for repeated achievements
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
                config.set(player.getUniqueId() + ".phantomsKilled", 0); // Reset count or set it to a higher threshold for repeated achievements
            }
            plugin.saveConfig();
        }
    }

    @EventHandler
    public void onPlayerBreakGrass(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.GRASS_BLOCK) {
            FileConfiguration config = plugin.getConfig();
            int grassBroken = config.getInt(player.getUniqueId() + ".grassBroken", 0) + 1;
            config.set(player.getUniqueId() + ".grassBroken", grassBroken);
            if (grassBroken >= 500) {
                awardAchievement(player, "Grass Grazer");
                config.set(player.getUniqueId() + ".grassBroken", 0); // Reset count or set it to a higher threshold for repeated achievements
            }
            plugin.saveConfig();
        }
    }

    @EventHandler
    public void onPlayerSurviveCreeper(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION && player.getHealth() > 0) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline() && !player.isDead()) {
                            awardAchievement(player, "Creeper Hug");
                        }
                    }
                }.runTaskLater(plugin, 20); // Check after 1 second (20 ticks)
            }
        }
    }

    @EventHandler
    public void onPlayerEatPork(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().getType() == Material.COOKED_PORKCHOP) {
            FileConfiguration config = plugin.getConfig();
            int porkEaten = config.getInt(player.getUniqueId() + ".porkEaten", 0) + 1;
            config.set(player.getUniqueId() + ".porkEaten", porkEaten);
            if (porkEaten >= 100) {
                awardAchievement(player, "Iron Chef");
                config.set(player.getUniqueId() + ".porkEaten", 0); // Reset count or set it to a higher threshold for repeated achievements
            }
            plugin.saveConfig();
        }
    }

    @EventHandler
    public void onPlayerStayAwake(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();
        long currentTime = System.currentTimeMillis();
        if (!awakeStartTime.containsKey(player.getUniqueId())) {
            awakeStartTime.put(player.getUniqueId(), currentTime);
        } else {
            long awakeTime = currentTime - awakeStartTime.get(player.getUniqueId());
            if (awakeTime >= 7200000) { // 3 in-game days
                awardAchievement(player, "Night Owl");
                awakeStartTime.put(player.getUniqueId(), currentTime); // Reset time or set it to a higher threshold for repeated achievements
            }
        }
    }

    @EventHandler
    public void onPlayerReadBook(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().getType() == Material.BOOK) {
            FileConfiguration config = plugin.getConfig();
            int booksRead = config.getInt(player.getUniqueId() + ".booksRead", 0) + 1;
            config.set(player.getUniqueId() + ".booksRead", booksRead);
            if (booksRead >= 50) {
                awardAchievement(player, "Bookworm");
                config.set(player.getUniqueId() + ".booksRead", 0); // Reset count or set it to a higher threshold for repeated achievements
            }
            plugin.saveConfig();
        }
    }

    @EventHandler
    public void onPlayerKillZombieBareHands(EntityDeathEvent event) {
        if (event.getEntity().getType() == EntityType.ZOMBIE && event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                FileConfiguration config = plugin.getConfig();
                int zombiesKilled = config.getInt(player.getUniqueId() + ".zombiesKilledBareHands", 0) + 1;
                config.set(player.getUniqueId() + ".zombiesKilledBareHands", zombiesKilled);
                if (zombiesKilled >= 50) {
                    awardAchievement(player, "Zombie Puncher");
                    config.set(player.getUniqueId() + ".zombiesKilledBareHands", 0); // Reset count or set it to a higher threshold for repeated achievements
                }
                plugin.saveConfig();
            }
        }
    }

    @EventHandler
    public void onPlayerVisitBiome(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Biome currentBiome = player.getLocation().getBlock().getBiome();
        visitedBiomes.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(currentBiome);
        if (visitedBiomes.get(player.getUniqueId()).size() >= 10) {
            awardAchievement(player, "Overworld Tourist");
            visitedBiomes.get(player.getUniqueId()).clear(); // Reset for repeated achievements
        }
    }

    @EventHandler
    public void onPlayerStargaze(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().getBlock().getLightFromSky() > 0) {
            if (!stargazerStartTime.containsKey(player.getUniqueId())) {
                stargazerStartTime.put(player.getUniqueId(), System.currentTimeMillis());
            } else {
                long stargazeTime = (System.currentTimeMillis() - stargazerStartTime.get(player.getUniqueId())) / 1000;
                if (stargazeTime >= 300) { // 5 minutes
                    awardAchievement(player, "Stargazer");
                    stargazerStartTime.remove(player.getUniqueId());
                }
            }
        } else {
            stargazerStartTime.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerShearSheep(PlayerShearEntityEvent event) {
        if (event.getEntity().getType() == EntityType.SHEEP) {
            Player player = event.getPlayer();
            FileConfiguration config = plugin.getConfig();
            int sheepSheared = config.getInt(player.getUniqueId() + ".sheepSheared", 0) + 1;
            config.set(player.getUniqueId() + ".sheepSheared", sheepSheared);
            if (sheepSheared >= 100) {
                awardAchievement(player, "Shear Luck");
                config.set(player.getUniqueId() + ".sheepSheared", 0); // Reset count or set it to a higher threshold for repeated achievements
            }
            plugin.saveConfig();
        }
    }

    private void awardAchievement(Player player, String achievement) {
        player.sendMessage(ChatColor.GOLD + "Achievement Unlocked: " + ChatColor.GREEN + achievement);
        String broadcastMessage = ChatColor.YELLOW + player.getName() + " has unlocked the achievement: " + achievement;
        Bukkit.getServer().broadcast(broadcastMessage, "sillyachievements.announce");
    }
}
