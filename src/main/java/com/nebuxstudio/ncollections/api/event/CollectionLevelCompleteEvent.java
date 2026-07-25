package com.nebuxstudio.ncollections.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player completes a collection level, regardless of trigger.
 * Purely informational (not cancellable) — the level is already marked
 * completed and the reward already granted or queued for claim by the time
 * this fires.
 */
public final class CollectionLevelCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String categoryId;
    private final String entryId;
    private final int level;
    private final boolean requiresClaim;

    public CollectionLevelCompleteEvent(Player player, String categoryId, String entryId,
                                        int level, boolean requiresClaim) {
        this.player = player;
        this.categoryId = categoryId;
        this.entryId = entryId;
        this.level = level;
        this.requiresClaim = requiresClaim;
    }

    public Player getPlayer() { return player; }
    public String getCategoryId() { return categoryId; }
    public String getEntryId() { return entryId; }
    public int getLevel() { return level; }

    /** True if the reward is waiting in /collections claim (or the GUI) instead of already granted. */
    public boolean isRequiresClaim() { return requiresClaim; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
