package fun.suya.syncAdvancements;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class SyncAdvancements extends JavaPlugin implements Listener {

    private final Set<Advancement> syncedAdvancements = new HashSet<>();

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

        // 检查玩家IP是否为127.x.x.x
        if (!isLocalhost(player) && !syncedAdvancements.contains(advancement)) {
            // 将完成的进度同步给其他玩家
            syncAdvancementToAllPlayers(advancement, player);
            syncedAdvancements.add(advancement); // 标记该进度已同步
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiningPlayer = event.getPlayer();

        // 检查新加入玩家IP是否为127.x.x.x
        if (isLocalhost(joiningPlayer)) {
            return;
        }

        syncedAdvancements.clear(); // 重置同步状态

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(joiningPlayer) && !isLocalhost(onlinePlayer)) {
                // 遍历进度
                Iterator<Advancement> advancements = Bukkit.getServer().advancementIterator();
                while (advancements.hasNext()) {
                    Advancement advancement = advancements.next();
                    // 新加入玩家同步其他玩家的进度
                    CheckAdvancement(onlinePlayer, joiningPlayer, advancement);
                    // 其他玩家同步新加入玩家的进度
                    CheckAdvancement(joiningPlayer, onlinePlayer, advancement);
                }
            }
        }
    }

    private void CheckAdvancement(Player Player1, Player Player2, Advancement advancement) {
        if (Player1.getAdvancementProgress(advancement).isDone() &&
                !Player2.getAdvancementProgress(advancement).isDone()) {
            if (advancement.getKey().getKey().startsWith("recipes/")) {
                Bukkit.broadcastMessage("§b已将 §a" + Player1.getName() + " §b取得的配方 §a" + advancement.getKey().getKey() + " §b同步至 §a" + Player2.getName());
            } else if (advancement.getKey().getKey().startsWith("adventure/")) {
                Bukkit.broadcastMessage("§b已将 §a" + Player1.getName() + " §b完成的进度 §a" + advancement.getKey().getKey() + " §b同步至 §a" + Player2.getName());
            } else {
                return;
            }
            for (String criteria : advancement.getCriteria()) {
                Player2.getAdvancementProgress(advancement).awardCriteria(criteria);
            }
        }
    }

    private void syncAdvancementToAllPlayers(Advancement advancement, Player sourcePlayer) {
        for (Player targetPlayer : Bukkit.getOnlinePlayers()) {
            if (!targetPlayer.equals(sourcePlayer) && !targetPlayer.getAdvancementProgress(advancement).isDone() && !isLocalhost(targetPlayer)) {
                if (advancement.getKey().getKey().startsWith("recipes/")) {
                    Bukkit.broadcastMessage("§b已将 §a" + sourcePlayer.getName() + " §b取得的配方 §a" + advancement.getKey().getKey() + " §b同步至 §a" + targetPlayer.getName());
                } else if (advancement.getKey().getKey().startsWith("adventure/")){
                    Bukkit.broadcastMessage("§b已将 §a" + sourcePlayer.getName() + " §b完成的进度 §a" + advancement.getKey().getKey() + " §b同步至 §a" + targetPlayer.getName());
                } else {
                    return;
                }
                // 如果目标玩家没有这个进度，授予他们
                for (String criteria : advancement.getCriteria()) {
                    targetPlayer.getAdvancementProgress(advancement).awardCriteria(criteria);
                }
            }
        }
    }

    private boolean isLocalhost(Player player) {
        try {
            String ip = player.getAddress().getAddress().getHostAddress();
            return ip.startsWith("127.");
        } catch (NullPointerException e) {
            Bukkit.broadcastMessage("§c无法判断玩家 §a" + player.getName() + " §c是否为假人！");
            return false;
        }
    }
}
