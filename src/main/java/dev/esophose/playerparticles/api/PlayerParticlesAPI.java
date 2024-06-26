package dev.esophose.playerparticles.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import dev.esophose.playerparticles.PlayerParticles;
import dev.esophose.playerparticles.manager.DataManager;
import dev.esophose.playerparticles.manager.GuiManager;
import dev.esophose.playerparticles.manager.ParticleGroupPresetManager;
import dev.esophose.playerparticles.manager.ParticleManager;
import dev.esophose.playerparticles.particles.ConsolePPlayer;
import dev.esophose.playerparticles.particles.FixedParticleEffect;
import dev.esophose.playerparticles.particles.PPlayer;
import dev.esophose.playerparticles.particles.ParticleEffect;
import dev.esophose.playerparticles.particles.ParticleGroup;
import dev.esophose.playerparticles.particles.ParticlePair;
import dev.esophose.playerparticles.particles.data.NoteColor;
import dev.esophose.playerparticles.particles.data.OrdinaryColor;
import dev.esophose.playerparticles.styles.ParticleStyle;

/**
 * The API for the PlayerParticles plugin.
 * Used to manipulate a player's particles and data.
 *
 * Note: This API will bypass all permissions and does not send any messages.
 *       Any changes made through the API will be saved to the database automatically.
 */
public final class PlayerParticlesAPI {

    private static PlayerParticlesAPI INSTANCE;

    private final PlayerParticles playerParticles;
    private final DataManager dataManager;

    private PlayerParticlesAPI() {
        this.playerParticles = PlayerParticles.getInstance();
        this.dataManager     = playerParticles.getManager(DataManager.class);
    }

    /**
     * @return the instance of the PlayerParticlesAPI
     */
    public static PlayerParticlesAPI getInstance() {
        if (INSTANCE == null)
            INSTANCE = new PlayerParticlesAPI();
        return INSTANCE;
    }

    /**
     * @return the currently installed version of the plugin
     */
    public String getVersion() {
        return this.playerParticles.getDescription().getVersion();
    }

    //region Get PPlayer

    /**
     * Gets a PPlayer from their UUID
     *
     * @param uuid The UUID of the PPlayer
     * @return The PPlayer, or null if not found
     */
    public PPlayer getPPlayer(UUID uuid) {
        return dataManager.getPPlayer(uuid);
    }

    /**
     * Gets a PPlayer from a Player
     *
     * @param player The Player
     * @return The PPlayer, or null if not found
     */
    public PPlayer getPPlayer(Player player) {
        Objects.requireNonNull(player);

        return this.getPPlayer(player.getUniqueId());
    }

    /**
     * Gets a PPlayer from a CommandSender
     *
     * @param sender The CommandSender, either a Player or ConsoleCommandSender
     * @return The PPlayer, or null if not found
     */
    public PPlayer getPPlayer(CommandSender sender) {
        Objects.requireNonNull(sender);

        if (sender instanceof Player) {
            return this.getPPlayer((Player) sender);
        } else if (sender instanceof ConsoleCommandSender) {
            return this.getConsolePPlayer();
        }

        return null;
    }

    /**
     * Gets the PPlayer representing the console
     *
     * @return The PPlayer, or null if not found
     */
    public PPlayer getConsolePPlayer() {
        return this.getPPlayer(ConsolePPlayer.getUUID());
    }

    //endregion

    //region Manage Active Player Particles

    /**
     * Adds an active particle to a Player's particles
     *
     * @param player The player to add to
     * @param particle The particle to add
     * @return The ParticlePair that was added or null if failed
     */
    public ParticlePair addActivePlayerParticle(Player player, ParticlePair particle) {
        Objects.requireNonNull(particle);

        PPlayer pplayer = this.getPPlayer(player);
        if (pplayer == null)
            return null;

        ParticleGroup particleGroup = pplayer.getActiveParticleGroup();
        if (particleGroup.getParticles().containsKey(particle.getId()))
            throw new IllegalArgumentException("A particle already exists with the id " + particle.getId());

        pplayer.getActiveParticleGroup().getParticles().put(particle.getId(), particle);
        dataManager.saveParticleGroup(player.getUniqueId(), pplayer.getActiveParticleGroup());
        return particle;
    }

    /**
     * Adds an active particle to a Player's particles
     *
     * @param player The player to add to
     * @param effect The effect of the particle
     * @param style The style of the particle
     * @return The ParticlePair that was added or null if failed
     */
    public ParticlePair addActivePlayerParticle(Player player, ParticleEffect effect, ParticleStyle style) {
        return this.addActivePlayerParticle(player, effect, style, null, null, null);
    }

    /**
     * Adds an active particle to a Player's particles
     *
     * @param player The player to add to
     * @param effect The effect of the particle
     * @param style The style of the particle
     * @param colorData The color data of the particle.
     * @return The ParticlePair that was added or null if failed
     */
    public ParticlePair addActivePlayerParticle(Player player, ParticleEffect effect, ParticleStyle style, OrdinaryColor colorData) {
        return this.addActivePlayerParticle(player, effect, style, colorData, null, null);
    }

    /**
     * Adds an active particle to a Player's particles
     *
     * @param player The player to add to
     * @param effect The effect of the particle
     * @param style The style of the particle
     * @param noteColorData The note color data of the particle
     * @return The ParticlePair that was added or null if failed
     */
    public ParticlePair addActivePlayerParticle(Player player, ParticleEffect effect, ParticleStyle style, NoteColor noteColorData) {
        return this.addActivePlayerParticle(player, effect, style, null, noteColorData, null);
    }

    /**
     * Adds an active particle to a Player's particles
     *
     * @param player The player to add to
     * @param effect The effect of the particle
     * @param style The style of the particle
     * @param materialData The material data of the particle
     * @return The ParticlePair that was added or null if failed
     */
    public ParticlePair addActivePlayerParticle(Player player, ParticleEffect effect, ParticleStyle style, Material materialData) {
        return this.addActivePlayerParticle(player, effect, style, null, null, materialData);
    }

    /**
     * @param player The player to add to
     * @param effect The effect of the particle
     * @param style The style of the particle
     * @param colorData The color data of the particle
     * @param noteColorData The note color data of the particle
     * @param materialData The material data of the particle
     * @return The ParticlePair that was added or null if failed
     */
    private ParticlePair addActivePlayerParticle(Player player, ParticleEffect effect, ParticleStyle style, OrdinaryColor colorData, NoteColor noteColorData, Material materialData) {
        Objects.requireNonNull(effect);
        Objects.requireNonNull(style);

        PPlayer pplayer = this.getPPlayer(player);
        if (pplayer == null)
            return null;

        Material itemMaterialData = null;
        Material blockMaterialData = null;
        if (materialData != null) {
            if (materialData.isBlock()) {
                blockMaterialData = materialData;
            } else {
                itemMaterialData = materialData;
            }
        }

        ParticlePair particle = new ParticlePair(player.getUniqueId(), pplayer.getNextActiveParticleId(), effect, style, itemMaterialData, blockMaterialData, colorData, noteColorData);
        this.addActivePlayerParticle(player, particle);
        return particle;
    }

    /**
     * Edits an active particle of a Player
     *
     * @param player The player to edit from
     * @param id The ID of the target particle
     * @param effect The new effect for the particle
     * @return The ParticlePair that was edited or null if failed
     */
    public ParticlePair editActivePlayerParticle(Player player, int id, ParticleEffect effect) {
        Objects.requireNonNull(effect);

        ParticleGroup group = this.validateActivePlayerParticle(player, id);
        if (group == null)
            return null;

        ParticlePair particle = group.getParticles().get(id);
        particle.setEffect(effect);
        dataManager.saveParticleGroup(player.getUniqueId(), group);
        return particle;
    }

    /**
     * Edits an active particle of a Player
     *
     * @param player The player to edit from
     * @param id The ID of the target particle
     * @param style The new style for the particle
     * @return The ParticlePair that was edited or null if failed
     */
    public ParticlePair editActivePlayerParticle(Player player, int id, ParticleStyle style) {
        Objects.requireNonNull(style);

        ParticleGroup group = this.validateActivePlayerParticle(player, id);
        if (group == null)
            return null;

        ParticlePair particle = group.getParticles().get(id);
        particle.setStyle(style);
        dataManager.saveParticleGroup(player.getUniqueId(), group);
        return particle;
    }

    /**
     * Edits an active particle of a Player
     *
     * @param player The player to edit from
     * @param id The ID of the target particle
     * @param colorData The new color data for the particle
     * @return The ParticlePair that was edited or null if failed
     */
    public ParticlePair editActivePlayerParticle(Player player, int id, OrdinaryColor colorData) {
        ParticleGroup group = this.validateActivePlayerParticle(player, id);
        if (group == null)
            return null;

        ParticlePair particle = group.getParticles().get(id);
        particle.setColor(colorData);
        dataManager.saveParticleGroup(player.getUniqueId(), group);
        return particle;
    }

    /**
     * Edits an active particle of a Player
     *
     * @param player The player to edit from
     * @param id The ID of the target particle
     * @param noteColorData The new note color data for the particle
     * @return The ParticlePair that was edited or null if failed
     */
    public ParticlePair editActivePlayerParticle(Player player, int id, NoteColor noteColorData) {
        ParticleGroup group = this.validateActivePlayerParticle(player, id);
        if (group == null)
            return null;

        ParticlePair particle = group.getParticles().get(id);
        particle.setNoteColor(noteColorData);
        dataManager.saveParticleGroup(player.getUniqueId(), group);
        return particle;
    }

    /**
     * Edits an active particle of a Player
     *
     * @param player The player to edit from
     * @param id The ID of the target particle
     * @param materialData The new material data for the particle
     * @return The ParticlePair that was edited or null if failed
     */
    public ParticlePair editActivePlayerParticle(Player player, int id, Material materialData) {
        ParticleGroup group = this.validateActivePlayerParticle(player, id);
        if (group == null)
            return null;

        ParticlePair particle = group.getParticles().get(id);
        if (materialData.isBlock()) {
            particle.setBlockMaterial(materialData);
        } else {
            particle.setItemMaterial(materialData);
        }
        dataManager.saveParticleGroup(player.getUniqueId(), group);
        return particle;
    }

    /**
     * Removes an active particle from a player by ID
     *
     * @param player The player to remove from
     * @param id The ID of the particle to remove
     * @return The ParticlePair that was removed or null if failed
     */
    public ParticlePair removeActivePlayerParticle(Player player, int id) {
        ParticleGroup group = this.validateActivePlayerParticle(player, id);
        if (group == null)
            return null;

        ParticlePair particle = group.getParticles().remove(id);
        dataManager.saveParticleGroup(player.getUniqueId(), group);
        return particle;
    }

    /**
     * Removes active particles from a player by effect
     *
     * @param player The player to remove from
     * @param effect The effect of the particle(s) to remove
     * @return A Set of removed ParticlePairs or null if failed
     */
    public Set<ParticlePair> removeActivePlayerParticles(Player player, ParticleEffect effect) {
        PPlayer pplayer = this.getPPlayer(player);
        if (pplayer == null)
            return null;

        ParticleGroup group = pplayer.getActiveParticleGroup();
        Set<ParticlePair> removedParticles = group.getParticles().values().stream().filter(x -> x.getEffect().equals(effect)).collect(Collectors.toSet());
        group.getParticles().values().removeAll(removedParticles);
        dataManager.saveParticleGroup(player.getUniqueId(), group);
        return removedParticles;
    }

    /**
     * Removes active particles from a player by style
     *
     * @param player The player to remove from
     * @param style The style of the particle(s) to remove
     * @return A Set of removed ParticlePairs or null if failed
     */
    public Set<ParticlePair> removeActivePlayerParticles(Player player, ParticleStyle style) {
        PPlayer pplayer = this.getPPlayer(player);
        if (pplayer == null)
            return null;

        ParticleGroup group = pplayer.getActiveParticleGroup();
        Set<ParticlePair> removedParticles = group.getParticles().values().stream().filter(x -> x.getStyle().equals(style)).collect(Collectors.toSet());
        group.getParticles().values().removeAll(removedParticles);
        dataManager.saveParticleGroup(player.getUniqueId(), group);
        return removedParticles;
    }

    /**
     * Ensures a particle with a given ID exists for a player
     *
     * @param player The player to check
     * @param id The ID of the particle
     * @return The active particle group for the player
     */
    private ParticleGroup validateActivePlayerParticle(Player player, int id) {
        PPlayer pplayer = this.getPPlayer(player);
        if (pplayer == null)
            return null;

        ParticleGroup particleGroup = pplayer.getActiveParticleGroup();
        if (!particleGroup.getParticles().containsKey(id))
            throw new IllegalArgumentException("No particle exists with the id " + id);

        return particleGroup;
    }

    /**
     * Removes all active particles from a player
     *
     * @param player The player to remove from
     * @return The number of particles removed or null if failed
     */
    public Integer resetActivePlayerParticles(Player player) {
        PPlayer pplayer = this.getPPlayer(player);
        if (pplayer == null)
            return null;

        int amount = pplayer.getActiveParticleGroup().getParticles().size();
        pplayer.getActiveParticleGroup().getParticles().clear();
        dataManager.saveParticleGroup(pplayer.getUniqueId(), pplayer.getActiveParticleGroup());
        return amount;
    }

    /**
     * Attempts to reset the active particles for the given player name.
     * This works even if the player is offline.
     *
     * @param playerName The name of the player to reset the active particles for
     * @param successConsumer The callback to execute when finished
     */
    public void resetActivePlayerParticles(String playerName, Consumer<Boolean> successConsumer) {
        if (successConsumer == null)
            successConsumer = success -> {};

        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            this.resetActivePlayerParticles(player);
            successConsumer.accept(true);
        } else {
            dataManager.resetActiveParticleGroup(playerName, successConsumer);
        }
    }

    /**
     * Gets all active particles from a player
     *
     * @param player The player to get from
     * @return A collection of the player's active particles
     */
    public Collection<ParticlePair> getActivePlayerParticles(Player player) {
        PPlayer pplayer = this.getPPlayer(player);
        if (pplayer == null)
            return new ArrayList<>();

        return pplayer.getActiveParticles();
    }

    /**
     * Gets an active particle from a player
     *
     * @param player The player to get from
     * @param id The ID of the particle to get
     * @return A particle or null if one doesn't exist
     */
    public ParticlePair getActivePlayerParticle(Player player, int id) {
        PPlayer pplayer = this.getPPlayer(player);
        if (pplayer == null)
            return null;

        return pplayer.getActiveParticle(id);
    }

    //endregion

    //region Manage Player Particle Groups

    /**
     * Saves a particle group to a player or edits an existing one
     *
     * @param player The player to save to
     * @param particleGroup The particle group to save
     * @return The ParticleGroup that was saved or null if failed
     */
    public ParticleGroup savePlayerParticleGroup(Player player, ParticleGroup particleGroup) {
        if (particleGroup.getParticles().isEmpty() && !particleGroup.getName().equals(ParticleGroup.DEFAULT_NAME))
            throw new IllegalArgumentException("Cannot save an empty ParticleGroup");

        PPlayer pplayer = this.getPPlayer(player);
        if (pplayer == null)
            return null;

        pplayer.getParticleGroups().put(particleGroup.getName().toLowerCase(), particleGroup);
        dataManager.saveParticleGroup(player.getUniqueId(), particleGroup);
        return particleGroup;
    }

    /**
     * Saves a particle group to a player or edits an existing one
     *
     * @param player The player to save to
     * @param groupName The name of the group to save
     * @param particles Particles that are part of the group
     * @return The ParticleGroup that was saved or null if failed
     */
    public ParticleGroup savePlayerParticleGroup(Player player, String groupName, Collection<ParticlePair> particles) {
        Map<Integer, ParticlePair> mappedParticles = new ConcurrentHashMap<>();
        particles.forEach(x -> mappedParticles.put(x.getId(), x));
        ParticleGroup particleGroup = new ParticleGroup(groupName.toLowerCase(), mappedParticles);
        return this.savePlayerParticleGroup(player, particleGroup);
    }

    /**
     * Removes a particle group from a player
     *
     * @param player The player to remove from
     * @param groupName The name of the particle group to remove
     * @return The ParticleGroup that was removed or null if failed
     */
    public ParticleGroup removePlayerParticleGroup(Player player, String groupName) {
        PPlayer pplayer = this.getPPlayer(player);
        if (pplayer == null)
            return null;

        ParticleGroup group = pplayer.getParticleGroupByName(groupName);
        if (group == null)
            throw new IllegalArgumentException("No group exists with the name " + groupName);

        pplayer.getParticleGroups().remove(groupName.toLowerCase());
        dataManager.removeParticleGroup(player.getUniqueId(), groupName);
        return group;
    }

    /**
     * Removes a particle group from a player
     *
     * @param player The player to remove from
     * @param particleGroup The particle group
     * @return The ParticleGroup that was removed or null if failed
     */
    public ParticleGroup removePlayerParticleGroup(Player player, ParticleGroup particleGroup) {
        return this.removePlayerParticleGroup(player, particleGroup.getName());
    }

    /**
     * Gets a collection of the player's particle groups
     *
     * @param player The player to get from
     * @return A collection of the player's particle groups
     */
    public Collection<ParticleGroup> getPlayerParticleGroups(Player player) {
        PPlayer pplayer = this.getPPlayer(player);
        if (pplayer == null)
            return new ArrayList<>();

        return pplayer.getParticleGroups().values();
    }
    
    public ParticleGroup loadParticleGroup(ConfigurationSection groupSection) {
        return playerParticles.getManager(ParticleGroupPresetManager.class).loadParticleGroup(groupSection);
    }

    public ParticleGroup loadParticleGroup(String groupName, ConfigurationSection groupSection) {
        return playerParticles.getManager(ParticleGroupPresetManager.class).loadParticleGroup(groupName, groupSection);
    }

    public ParticlePair loadParticlePair(int id, ConfigurationSection particleSection) {
        return playerParticles.getManager(ParticleGroupPresetManager.class).loadParticlePair(id, particleSection);
    }

    //endregion

    //region Fixed Effect Management

    /**
     * Creates a fixed particle effect for a player
     *
     * @param sender The sender to create for, either a Player or ConsoleCommandSender
     * @param fixedEffect The FixedParticleEffect
     * @return The FixedParticleEffect that was created or null if failed
     */
    public FixedParticleEffect createFixedParticleEffect(CommandSender sender, FixedParticleEffect fixedEffect) {
        PPlayer pplayer = this.getPPlayer(sender);
        if (pplayer == null)
            return null;

        if (pplayer.getFixedEffectIds().contains(fixedEffect.getId()))
            throw new IllegalArgumentException("A fixed effect already exists with the id " + fixedEffect.getId());

        pplayer.addFixedEffect(fixedEffect);
        dataManager.saveFixedEffect(fixedEffect);
        return fixedEffect;
    }

    /**
     * Creates a fixed particle effect for a player
     *
     * @param sender The sender to create for, either a Player or ConsoleCommandSender
     * @param location The location to create at
     * @param particle The particle to display
     * @return The FixedParticleEffect that was created or null if failed
     */
    public FixedParticleEffect createFixedParticleEffect(CommandSender sender, Location location, ParticlePair particle) {
        PPlayer pplayer = this.getPPlayer(sender);
        if (pplayer == null)
            return null;

        FixedParticleEffect fixedEffect = new FixedParticleEffect(pplayer.getUniqueId(), pplayer.getNextFixedEffectId(), location, particle);
        return this.createFixedParticleEffect(sender, fixedEffect);
    }

    /**
     * Creates a fixed particle effect for a player
     *
     * @param sender The sender to create for, either a Player or ConsoleCommandSender
     * @param location The location to create at
     * @param effect The effect of the particle
     * @param style The style of the particle
     * @return The FixedParticleEffect that was created or null if failed
     */
    public FixedParticleEffect createFixedParticleEffect(CommandSender sender, Location location, ParticleEffect effect, ParticleStyle style) {
        return this.createFixedParticleEffect(sender, location, effect, style, null, null, null);
    }

    /**
     * Creates a fixed particle effect for a player
     *
     * @param sender The sender to create for, either a Player or ConsoleCommandSender
     * @param location The location to create at
     * @param effect The effect of the particle
     * @param style The style of the particle
     * @param colorData The color data of the particle
     * @return The FixedParticleEffect that was created or null if failed
     */
    public FixedParticleEffect createFixedParticleEffect(CommandSender sender, Location location, ParticleEffect effect, ParticleStyle style, OrdinaryColor colorData) {
        return this.createFixedParticleEffect(sender, location, effect, style, colorData, null, null);
    }

    /**
     * Creates a fixed particle effect for a player
     *
     * @param sender The sender to create for, either a Player or ConsoleCommandSender
     * @param location The location to create at
     * @param effect The effect of the particle
     * @param style The style of the particle
     * @param noteColorData The note color data of the particle
     * @return The FixedParticleEffect that was created or null if failed
     */
    public FixedParticleEffect createFixedParticleEffect(CommandSender sender, Location location, ParticleEffect effect, ParticleStyle style, NoteColor noteColorData) {
        return this.createFixedParticleEffect(sender, location, effect, style, null, noteColorData, null);
    }

    /**
     * Creates a fixed particle effect for a player
     *
     * @param sender The sender to create for, either a Player or ConsoleCommandSender
     * @param location The location to create at
     * @param effect The effect of the particle
     * @param style The style of the particle
     * @param materialData The material data of the particle
     * @return The FixedParticleEffect that was created or null if failed
     */
    public FixedParticleEffect createFixedParticleEffect(CommandSender sender, Location location, ParticleEffect effect, ParticleStyle style, Material materialData) {
        return this.createFixedParticleEffect(sender, location, effect, style, null, null, materialData);
    }

    /**
     * Creates a fixed particle effect for a player
     *
     * @param sender The sender to create for, either a Player or ConsoleCommandSender
     * @param location The location to create at
     * @param effect The effect of the particle
     * @param style The style of the particle
     * @param colorData The color data of the particle
     * @param noteColorData The note color data of the particle
     * @param materialData The material data of the particle
     * @return The FixedParticleEffect that was created or null if failed
     */
    private FixedParticleEffect createFixedParticleEffect(CommandSender sender, Location location, ParticleEffect effect, ParticleStyle style, OrdinaryColor colorData, NoteColor noteColorData, Material materialData) {
        PPlayer pplayer = this.getPPlayer(sender);
        if (pplayer == null)
            return null;

        Material itemMaterialData = null;
        Material blockMaterialData = null;
        if (materialData != null) {
            if (materialData.isBlock()) {
                blockMaterialData = materialData;
            } else {
                itemMaterialData = materialData;
            }
        }

        ParticlePair particle = new ParticlePair(pplayer.getUniqueId(), 1, effect, style, itemMaterialData, blockMaterialData, colorData, noteColorData);
        return this.createFixedParticleEffect(sender, location, particle);
    }

    /**
     * Edits a fixed particle effect for a player
     *
     * @param sender The sender to edit from, either a Player or ConsoleCommandSender
     * @param fixedEffect The modified fixed effect to edit
     * @return The FixedParticleEffect that was edited or null if failed
     */
    public FixedParticleEffect editFixedParticleEffect(CommandSender sender, FixedParticleEffect fixedEffect) {
        PPlayer pplayer = this.getPPlayer(sender);
        if (pplayer == null)
            return null;

        if (this.validateFixedParticleEffect(sender, fixedEffect.getId()) == null)
            return null;

        pplayer.removeFixedEffect(fixedEffect.getId());
        pplayer.addFixedEffect(fixedEffect);
        dataManager.updateFixedEffect(fixedEffect);
        return fixedEffect;
    }

    /**
     * Edits a fixed particle effect for a player
     *
     * @param sender The sender to edit from, either a Player or ConsoleCommandSender
     * @param id The ID of the fixed particle effect
     * @param location The new location
     * @return The FixedParticleEffect that was edited or null if failed
     */
    public FixedParticleEffect editFixedParticleEffect(CommandSender sender, int id, Location location) {
        FixedParticleEffect fixedEffect = this.validateFixedParticleEffect(sender, id);
        if (fixedEffect == null)
            return null;

        fixedEffect.setCoordinates(location.getX(), location.getY(), location.getZ());
        dataManager.saveFixedEffect(fixedEffect);
        return fixedEffect;
    }

    /**
     * Edits a fixed particle effect for a player
     *
     * @param sender The sender to edit from, either a Player or ConsoleCommandSender
     * @param id The ID of the fixed particle effect
     * @param effect The new effect
     * @return The FixedParticleEffect that was edited or null if failed
     */
    public FixedParticleEffect editFixedParticleEffect(CommandSender sender, int id, ParticleEffect effect) {
        FixedParticleEffect fixedEffect = this.validateFixedParticleEffect(sender, id);
        if (fixedEffect == null)
            return null;

        fixedEffect.getParticlePair().setEffect(effect);
        dataManager.saveFixedEffect(fixedEffect);
        return fixedEffect;
    }

    /**
     * Edits a fixed particle effect for a player
     *
     * @param sender The sender to edit from, either a Player or ConsoleCommandSender
     * @param id The ID of the fixed particle effect
     * @param style The new style
     * @return The FixedParticleEffect that was edited or null if failed
     */
    public FixedParticleEffect editFixedParticleEffect(CommandSender sender, int id, ParticleStyle style) {
        FixedParticleEffect fixedEffect = this.validateFixedParticleEffect(sender, id);
        if (fixedEffect == null)
            return null;

        fixedEffect.getParticlePair().setStyle(style);
        dataManager.saveFixedEffect(fixedEffect);
        return fixedEffect;
    }

    /**
     * Edits a fixed particle effect for a player
     *
     * @param sender The sender to edit from, either a Player or ConsoleCommandSender
     * @param id The ID of the fixed particle effect
     * @param colorData The new color data
     * @return The FixedParticleEffect that was edited or null if failed
     */
    public FixedParticleEffect editFixedParticleEffect(CommandSender sender, int id, OrdinaryColor colorData) {
        FixedParticleEffect fixedEffect = this.validateFixedParticleEffect(sender, id);
        if (fixedEffect == null)
            return null;

        fixedEffect.getParticlePair().setColor(colorData);
        dataManager.saveFixedEffect(fixedEffect);
        return fixedEffect;
    }

    /**
     * Edits a fixed particle effect for a player
     *
     * @param sender The sender to edit from, either a Player or ConsoleCommandSender
     * @param id The ID of the fixed particle effect
     * @param noteColorData The new note color data
     * @return The FixedParticleEffect that was edited or null if failed
     */
    public FixedParticleEffect editFixedParticleEffect(CommandSender sender, int id, NoteColor noteColorData) {
        FixedParticleEffect fixedEffect = this.validateFixedParticleEffect(sender, id);
        if (fixedEffect == null)
            return null;

        fixedEffect.getParticlePair().setNoteColor(noteColorData);
        dataManager.saveFixedEffect(fixedEffect);
        return fixedEffect;
    }

    /**
     * Edits a fixed particle effect for a player
     *
     * @param sender The sender to edit from, either a Player or ConsoleCommandSender
     * @param id The ID of the fixed particle effect
     * @param materialData The new material data
     * @return The FixedParticleEffect that was edited or null if failed
     */
    public FixedParticleEffect editFixedParticleEffect(CommandSender sender, int id, Material materialData) {
        FixedParticleEffect fixedEffect = this.validateFixedParticleEffect(sender, id);
        if (fixedEffect == null)
            return null;

        if (materialData.isBlock()) {
            fixedEffect.getParticlePair().setBlockMaterial(materialData);
        } else {
            fixedEffect.getParticlePair().setItemMaterial(materialData);
        }
        dataManager.saveFixedEffect(fixedEffect);
        return fixedEffect;
    }

    /**
     * Removes a fixed particle effect from a player
     *
     * @param sender The sender to remove from, either a Player or ConsoleCommandSender
     * @param id The ID of the fixed particle effect
     * @return The FixedParticleEffect that was removed or null if failed
     */
    public FixedParticleEffect removeFixedEffect(CommandSender sender, int id) {
        FixedParticleEffect fixedEffect = this.validateFixedParticleEffect(sender, id);
        if (fixedEffect == null)
            return null;

        PPlayer pplayer = this.getPPlayer(sender);
        if (pplayer == null)
            return null;

        pplayer.removeFixedEffect(id);
        dataManager.removeFixedEffect(pplayer.getUniqueId(), fixedEffect.getId());
        return fixedEffect;
    }

    /**
     * Removes fixed effects within a given radius of a location
     *
     * @param location The location to search around
     * @param radius The radius to remove
     * @return The number of fixed effects that were removed
     */
    public int removeFixedEffectsInRange(Location location, double radius) {
        ParticleManager particleManager = this.playerParticles.getManager(ParticleManager.class);

        int removedAmount = 0;
        for (PPlayer pplayer : particleManager.getPPlayers()) {
            Set<Integer> removedIds = new HashSet<>();
            for (FixedParticleEffect fixedEffect : pplayer.getFixedParticles())
                if (fixedEffect.getLocation().getWorld() == location.getWorld() && fixedEffect.getLocation().distance(location) <= radius)
                    removedIds.add(fixedEffect.getId());
            for (int id : removedIds) {
                dataManager.removeFixedEffect(pplayer.getUniqueId(), id);
                pplayer.removeFixedEffect(id);
            }
            removedAmount += removedIds.size();
        }

        return removedAmount;
    }

    /**
     * Validates that a fixed particle effect with the given ID exists for a player
     *
     * @param sender The sender to check, either a Player or CommandSender
     * @param id The ID of the fixed particle effect
     * @return The fixed particle effect
     */
    private FixedParticleEffect validateFixedParticleEffect(CommandSender sender, int id) {
        PPlayer pplayer = this.getPPlayer(sender);
        if (pplayer == null)
            return null;

        FixedParticleEffect fixedEffect = pplayer.getFixedEffectById(id);
        if (fixedEffect == null)
            throw new IllegalArgumentException("No fixed effect exists with the id " + id);

        return fixedEffect;
    }

    /**
     * Gets a fixed particle effect for a player
     *
     * @param sender The sender to get from, either a Player or CommandSender
     * @param id The ID of the fixed particle effect
     * @return The fixed particle effect, or null if not found
     */
    public FixedParticleEffect getFixedParticleEffect(CommandSender sender, int id) {
        PPlayer pplayer = this.getPPlayer(sender);
        if (pplayer == null)
            return null;

        return pplayer.getFixedEffectById(id);
    }

    /**
     * Gets a collection of a player's fixed particle effects
     *
     * @param sender The sender to get from, either a Player or CommandSender
     * @return A collection of the player's fixed particle effects
     */
    public Collection<FixedParticleEffect> getFixedParticleEffects(CommandSender sender) {
        PPlayer pplayer = this.getPPlayer(sender);
        if (pplayer == null)
            return new ArrayList<>();

        return pplayer.getFixedParticlesMap().values();
    }

    //endregion

    //region GUI Management

    /**
     * Opens the particles gui for a player
     *
     * @param player The player to open the gui for
     */
    public void openParticlesGui(Player player) {
        PPlayer pplayer = this.getPPlayer(player);
        if (pplayer == null)
            return;

        this.playerParticles.getManager(GuiManager.class).openDefault(pplayer);
    }

    //endregion

    //region Player Settings

    /**
     * Toggles a player's particle visibility on/off
     *
     * @param player The player to toggle visibility for
     * @param particlesHidden true if the particles should be hidden, or false for visible
     */
    public void togglePlayerParticleVisibility(Player player, boolean particlesHidden) {
        PPlayer pplayer = this.getPPlayer(player);
        if (pplayer == null)
            return;

        pplayer.setParticlesHidden(particlesHidden);
        dataManager.updateSettingParticlesHidden(player.getUniqueId(), particlesHidden);
    }

    //endregion

}
