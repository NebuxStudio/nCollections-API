package com.nebuxstudio.ncollections.api;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

/**
 * The real contract of the nCollections API. This interface lives entirely
 * in the {@code nCollections-api} module — no implementation, no internal
 * classes, nothing sensitive. The nCollections plugin implements it and
 * registers the implementation via Bukkit's {@link org.bukkit.plugin.ServicesManager}
 * when it enables; {@link nCollectionsAPI} is the thin static facade most
 * callers should use instead of looking this up by hand.
 *
 * THREAD SAFETY: read methods (getProgress, isLevelCompleted, getCurrentLevel,
 * isMaxed, hasPendingClaim, getAllProgress, isDataLoaded, getCategories,
 * getEntries, entryExists) are safe to call from any thread. Write methods
 * (addProgress, setProgress, resetEntry, resetAll, claimLevel, claimAll,
 * openMenu) always hop onto the correct thread for the target player
 * internally (the main thread on Bukkit/Spigot/Paper/Purpur, that player's
 * region thread on Folia) before touching anything — safe to call from an
 * async task too. Because of that hop, their effects may not be visible the
 * instant the call returns; the boolean return values only report whether
 * the request itself was valid, not that it already ran.
 */
public interface nCollectionsService {

    // ------------------------------------------------------------------
    // Writing progress
    // ------------------------------------------------------------------

    /**
     * Adds progress to a collection entry for a player.
     *
     * @return true if the category/entry exist (a listener may still reduce
     *         the amount to 0 or cancel it entirely via CollectionProgressEvent
     *         once it runs — this only reports whether the request was valid)
     */
    boolean addProgress(Player player, String categoryId, String entryId, long amount);

    /**
     * Directly sets a player's progress for an entry (instead of adding to
     * it) — for data imports or syncing state from another system. Raising
     * the value still runs the normal level-completion checks; lowering it
     * never un-completes an already-earned level.
     */
    boolean setProgress(Player player, String categoryId, String entryId, long amount);

    /** Wipes progress, completed levels, and pending claims for ONE entry. Cannot be undone. */
    boolean resetEntry(Player player, String categoryId, String entryId);

    /** Wipes every collection a player has ever touched. Cannot be undone. */
    boolean resetAll(Player player);

    // ------------------------------------------------------------------
    // Claiming rewards
    // ------------------------------------------------------------------

    /** Claims a single level's pending reward, if there is one waiting. */
    boolean claimLevel(Player player, String categoryId, String entryId, int level);

    /** Claims every pending reward this player has across every category. */
    boolean claimAll(Player player);

    // ------------------------------------------------------------------
    // Reading progress
    // ------------------------------------------------------------------

    /** Current cumulative amount collected by a player for this entry (0 if never collected/unknown). */
    long getProgress(Player player, String categoryId, String entryId);

    /** Whether the player has completed a specific level of this entry. */
    boolean isLevelCompleted(Player player, String categoryId, String entryId, int level);

    /** Highest level the player has completed for this entry (0 if none yet). */
    int getCurrentLevel(Player player, String categoryId, String entryId);

    /** Highest level number defined for this entry in collections.yml. */
    int getMaxLevel(String categoryId, String entryId);

    /** Whether the player has completed every level of this entry. */
    boolean isMaxed(Player player, String categoryId, String entryId);

    /** Whether a specific level's reward is sitting unclaimed for this player. */
    boolean hasPendingClaim(Player player, String categoryId, String entryId, int level);

    /** Whether the category/entry combination exists in the loaded collections.yml. */
    boolean entryExists(String categoryId, String entryId);

    /** Whether this player's collection data has finished loading (false right after join, briefly). */
    boolean isDataLoaded(Player player);

    /** Every category id currently defined in collections.yml. */
    Set<String> getCategories();

    /** Every entry id defined within a category (empty if the category doesn't exist). */
    Set<String> getEntries(String categoryId);

    /** A snapshot of every entry this player has ANY progress in, keyed "category.entry" -> amount. */
    Map<String, Long> getAllProgress(Player player);

    // ------------------------------------------------------------------
    // GUI
    // ------------------------------------------------------------------

    /** Opens the categories menu for this player. */
    void openMenu(Player player);

    /** Opens a specific category's entries menu directly, same as `/collections <category>`. */
    void openMenu(Player player, String categoryId);

    // ------------------------------------------------------------------
    // Leaderboards
    // ------------------------------------------------------------------

    /**
     * Top players for a single collection entry, ranked by amount collected,
     * highest first. Player names are resolved fresh at query time (see
     * {@link LeaderboardEntry}), never cached by nCollections itself.
     *
     * Backed by a single sorted query on SQLite/MySQL; on YAML storage this
     * scans every player file on disk, so it's cached internally (see
     * config.yml -> leaderboards.cache-seconds) rather than hitting disk on
     * every call. Safe to call from any thread; the future completes off
     * the calling thread.
     */
    java.util.concurrent.CompletableFuture<java.util.List<LeaderboardEntry>> getEntryLeaderboard(
            String categoryId, String entryId, int limit);

    /**
     * Top players for an entire category, ranked by the SUM of everything
     * they've collected across every entry in it. Same caching/performance
     * notes as {@link #getEntryLeaderboard(String, String, int)}.
     */
    java.util.concurrent.CompletableFuture<java.util.List<LeaderboardEntry>> getCategoryLeaderboard(
            String categoryId, int limit);
}
