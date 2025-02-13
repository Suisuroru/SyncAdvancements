package fun.suya.syncAdvancements;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;

public final class SyncAdvancements extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin setup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        Advancement advancement = event.getAdvancement();

        // 将完成的进度同步给其他玩家
        syncAdvancementToAllPlayers(advancement, player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiningPlayer = event.getPlayer();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(joiningPlayer)) {
                // 遍历进度
                Iterator<Advancement> advancements = Bukkit.getServer().advancementIterator();
                while (advancements.hasNext()) {
                    Advancement advancement = advancements.next();


                    // 新加入玩家同步其他玩家的进度
                    if (onlinePlayer.getAdvancementProgress(advancement).isDone() &&
                            !joiningPlayer.getAdvancementProgress(advancement).isDone()) {
                        for (String criteria : advancement.getCriteria()) {
                            joiningPlayer.getAdvancementProgress(advancement).awardCriteria(criteria);
                        }
                        if (advancement.getKey().getKey().startsWith("recipes/")) {
                            Bukkit.broadcastMessage("§a" + onlinePlayer.getName() + " §b已经取得配方 §a" + advancement.getKey().getKey());
                        } else {
                            Bukkit.broadcastMessage("§a" + onlinePlayer.getName() + " §b已经完成进度 §a" + advancement.getKey().getKey());
                        }
                    }

                    // 其他玩家同步新加入玩家的进度
                    if (joiningPlayer.getAdvancementProgress(advancement).isDone() &&
                            !onlinePlayer.getAdvancementProgress(advancement).isDone()) {
                        for (String criteria : advancement.getCriteria()) {
                            onlinePlayer.getAdvancementProgress(advancement).awardCriteria(criteria);
                        }
                        if (advancement.getKey().getKey().startsWith("recipes/")) {
                            Bukkit.broadcastMessage("§a" + joiningPlayer.getName() + " §b已经取得配方 §a" + advancement.getKey().getKey());
                        } else {
                            Bukkit.broadcastMessage("§a" + joiningPlayer.getName() + " §b已经完成进度 §a" + advancement.getKey().getKey());
                        }
                    }
                }
            }
        }
    }

    private void syncAdvancementToAllPlayers(Advancement advancement, Player sourcePlayer) {

        for (Player targetPlayer : Bukkit.getOnlinePlayers()) {
            if (!targetPlayer.equals(sourcePlayer) && !targetPlayer.getAdvancementProgress(advancement).isDone()) {
                // 如果目标玩家没有这个进度，授予他们
                for (String criteria : advancement.getCriteria()) {
                    targetPlayer.getAdvancementProgress(advancement).awardCriteria(criteria);
                }
                if (advancement.getKey().getKey().startsWith("recipes/")) {
                    Bukkit.broadcastMessage("§a" + sourcePlayer.getName() + " §b取得配方 §a" + advancement.getKey().getKey());
                } else {
                    Bukkit.broadcastMessage("§a" + sourcePlayer.getName() + " §b完成进度 §a" + advancement.getKey().getKey());
                }
            }
        }
    }
}
