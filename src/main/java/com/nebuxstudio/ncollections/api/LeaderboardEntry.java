package com.nebuxstudio.ncollections.api;

import java.util.UUID;

/**
 * One row of a leaderboard result: a player and their amount, already
 * resolved and ready to display. Pure data, no logic — safe to live in the
 * api module like everything else here.
 *
 * {@code name} is resolved via Bukkit's own offline-player cache at query
 * time (never stored by nCollections itself), so it always reflects the
 * player's current name — falls back to their UUID as a string in the rare
 * case Bukkit has no cached name for them yet.
 */
public record LeaderboardEntry(UUID uuid, String name, long amount) {
}
