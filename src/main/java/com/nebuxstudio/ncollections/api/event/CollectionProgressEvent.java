package com.nebuxstudio.ncollections.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired right before progress is added to a collection entry, regardless of
 * trigger (block break, farming, fishing, entity kill, or the public API).
 * Other plugins can cancel it to block the progress entirely, or change the
 * amount before it's applied (e.g. a booster plugin doubling collection
 * gains for VIP players).
 */
public final class CollectionProgressEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String categoryId;
    private final String entryId;
    private long amount;
    private boolean cancelled;

    public CollectionProgressEvent(Player player, String categoryId, String entryId, long amount) {
        this.player = player;
        this.categoryId = categoryId;
        this.entryId = entryId;
        this.amount = amount;
    }

    public Player getPlayer() { return player; }
    public String getCategoryId() { return categoryId; }
    public String getEntryId() { return entryId; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
