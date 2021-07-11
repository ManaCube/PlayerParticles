package dev.esophose.playerparticles.particles.listener;

import dev.esophose.playerparticles.PlayerParticles;
import dev.esophose.playerparticles.manager.ConfigurationManager.Setting;
import dev.esophose.playerparticles.manager.DataManager;
import dev.esophose.playerparticles.objects.Pair;
import dev.esophose.playerparticles.particles.PPlayer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class PPlayerMovementListener implements Listener {
    
    private static final int                                        CHECK_INTERVAL = 3;
    private              Int2ObjectOpenHashMap<Pair<Integer, UUID>> timeSinceLastMovement;
    private              Int2ObjectOpenHashMap<Vector>              previousVectors;
    
    public PPlayerMovementListener() {
        DataManager dataManager = PlayerParticles.getInstance().getManager(DataManager.class);
        this.timeSinceLastMovement = new Int2ObjectOpenHashMap<>();
        this.previousVectors = new Int2ObjectOpenHashMap<>();

        Bukkit.getScheduler().runTaskTimer(PlayerParticles.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                int      playerId        = player.getEntityId();
                UUID     playerUUID      = player.getUniqueId();
                Location currentLocation = player.getLocation();
                Vector   previousVector  = this.previousVectors.get(playerId);
                Vector   currentVector   = new Vector(currentLocation.getBlockX(), currentLocation.getBlockY(), currentLocation.getBlockZ());

                this.previousVectors.put(playerId, currentVector);

                if (previousVector == null || !previousVector.equals(currentVector)) {
                    if (!this.timeSinceLastMovement.containsKey(playerId)) {
                        this.timeSinceLastMovement.put(playerId, new Pair<>(0, playerUUID));
                    } else {
                        this.timeSinceLastMovement.replace(playerId, new Pair<>(0, playerUUID));
                    }
                }
            }

            List<Integer> toRemove = new ArrayList<>();

            for (Int2ObjectMap.Entry<Pair<Integer, UUID>> entryPair : this.timeSinceLastMovement.int2ObjectEntrySet()) {
                Pair<Integer, UUID> pair     = entryPair.getValue();
                int                 playerId = entryPair.getIntKey();
                UUID                uuid     = pair.getValue();
                PPlayer             pplayer  = dataManager.getPPlayer(uuid);

                if (pplayer == null) {
                    toRemove.add(playerId);
                } else {
                    int standingTime = pair.getKey();

                    pplayer.setMoving(standingTime < Setting.TOGGLE_ON_MOVE_DELAY.getInt());

                    if (pplayer.isMoving())
                        this.timeSinceLastMovement.replace(playerId, new Pair<>(standingTime + CHECK_INTERVAL, uuid));
                }
            }

            for (int playerId : toRemove)
                this.timeSinceLastMovement.remove(playerId);
        }, 0, CHECK_INTERVAL);
    }

}
