package com.nebuxstudio.ncollections.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Map;
import java.util.Set;

/**
 * Static facade for {@link nCollectionsService} — this is what other plugins
 * should actually call. Every method here just looks up the live
 * implementation via Bukkit's {@link org.bukkit.plugin.ServicesManager} and
 * delegates to it, failing safely (false/0/empty/no-op) if nCollections
 * isn't installed or hasn't finished enabling yet. No implementation logic
 * lives here or anywhere in this module — see {@link nCollectionsService}'s
 * javadoc for the full contract and thread-safety notes.
 *
 * Usage from another plugin:
 * <pre>{@code
 * // plugin.yml: softdepend: [nCollections]
 * // pom.xml: nCollections-api as a "provided" (compileOnly) dependency
 * NCollectionsAPI.addProgress(player, "mining", "iron", 1);
 * }</pre>
 *
 * See also {@link com.nebuxstudio.ncollections.api.event.CollectionProgressEvent}
 * and {@link com.nebuxstudio.ncollections.api.event.CollectionLevelCompleteEvent}
 * if you'd rather react to progress/completions than cause them.
 */
public final class nCollectionsAPI {

    private nCollectionsAPI() {}

    /** The live service instance, or null if nCollections isn't installed/enabled. */
    public static nCollectionsService get() {
        RegisteredServiceProvider<nCollectionsService> provider =
                Bukkit.getServicesManager().getRegistration(nCollectionsService.class);
        return provider == null ? null : provider.getProvider();
    }

    // ------------------------------------------------------------------
    // Writing progress
    // ------------------------------------------------------------------

    public static boolean addProgress(Player player, String categoryId, String entryId, long amount) {
        nCollectionsService service = get();
        return service != null && service.addProgress(player, categoryId, entryId, amount);
    }

    public static boolean setProgress(Player player, String categoryId, String entryId, long amount) {
        nCollectionsService service = get();
        return service != null && service.setProgress(player, categoryId, entryId, amount);
    }

    public static boolean resetEntry(Player player, String categoryId, String entryId) {
        nCollectionsService service = get();
        return service != null && service.resetEntry(player, categoryId, entryId);
    }

    public static boolean resetAll(Player player) {
        nCollectionsService service = get();
        return service != null && service.resetAll(player);
    }

    // ------------------------------------------------------------------
    // Claiming rewards
    // ------------------------------------------------------------------

    public static boolean claimLevel(Player player, String categoryId, String entryId, int level) {
        nCollectionsService service = get();
        return service != null && service.claimLevel(player, categoryId, entryId, level);
    }

    public static boolean claimAll(Player player) {
        nCollectionsService service = get();
        return service != null && service.claimAll(player);
    }

    // ------------------------------------------------------------------
    // Reading progress
    // ------------------------------------------------------------------

    public static long getProgress(Player player, String categoryId, String entryId) {
        nCollectionsService service = get();
        return service == null ? 0L : service.getProgress(player, categoryId, entryId);
    }

    public static boolean isLevelCompleted(Player player, String categoryId, String entryId, int level) {
        nCollectionsService service = get();
        return service != null && service.isLevelCompleted(player, categoryId, entryId, level);
    }

    public static int getCurrentLevel(Player player, String categoryId, String entryId) {
        nCollectionsService service = get();
        return service == null ? 0 : service.getCurrentLevel(player, categoryId, entryId);
    }

    public static int getMaxLevel(String categoryId, String entryId) {
        nCollectionsService service = get();
        return service == null ? 0 : service.getMaxLevel(categoryId, entryId);
    }

    public static boolean isMaxed(Player player, String categoryId, String entryId) {
        nCollectionsService service = get();
        return service != null && service.isMaxed(player, categoryId, entryId);
    }

    public static boolean hasPendingClaim(Player player, String categoryId, String entryId, int level) {
        nCollectionsService service = get();
        return service != null && service.hasPendingClaim(player, categoryId, entryId, level);
    }

    public static boolean entryExists(String categoryId, String entryId) {
        nCollectionsService service = get();
        return service != null && service.entryExists(categoryId, entryId);
    }

    public static boolean isDataLoaded(Player player) {
        nCollectionsService service = get();
        return service != null && service.isDataLoaded(player);
    }

    public static Set<String> getCategories() {
        nCollectionsService service = get();
        return service == null ? Set.of() : service.getCategories();
    }

    public static Set<String> getEntries(String categoryId) {
        nCollectionsService service = get();
        return service == null ? Set.of() : service.getEntries(categoryId);
    }

    public static Map<String, Long> getAllProgress(Player player) {
        nCollectionsService service = get();
        return service == null ? Map.of() : service.getAllProgress(player);
    }

    // ------------------------------------------------------------------
    // GUI
    // ------------------------------------------------------------------

    public static void openMenu(Player player) {
        nCollectionsService service = get();
        if (service != null) service.openMenu(player);
    }

    public static void openMenu(Player player, String categoryId) {
        nCollectionsService service = get();
        if (service != null) service.openMenu(player, categoryId);
    }

    // ------------------------------------------------------------------
    // Leaderboards
    // ------------------------------------------------------------------

    public static java.util.concurrent.CompletableFuture<java.util.List<LeaderboardEntry>> getEntryLeaderboard(
            String categoryId, String entryId, int limit) {
        nCollectionsService service = get();
        return service == null
                ? java.util.concurrent.CompletableFuture.completedFuture(java.util.List.of())
                : service.getEntryLeaderboard(categoryId, entryId, limit);
    }

    public static java.util.concurrent.CompletableFuture<java.util.List<LeaderboardEntry>> getCategoryLeaderboard(
            String categoryId, int limit) {
        nCollectionsService service = get();
        return service == null
                ? java.util.concurrent.CompletableFuture.completedFuture(java.util.List.of())
                : service.getCategoryLeaderboard(categoryId, limit);
    }
}
