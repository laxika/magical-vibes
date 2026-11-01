package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A game's rules events that the trigger pipeline hasn't taken yet, grouped into batches, plus what the
 * log needs to know to describe zone changes. Held by {@code GameData} and copied with it for simulations
 * ({@link #copyFrom}); accessed under the game's monitor like the rest of {@code GameData}.
 *
 * <h2>Batches</h2>
 * Between {@link #beginBatch} and the matching {@link #endBatch} every event joins one batch, the events of
 * one occurrence (CR 603.2c). Scopes nest; only the outermost closes the batch. An event emitted outside a
 * scope is a batch of its own. {@link #drain} hands the closed batches to the trigger pipeline.
 *
 * <h2>Zone changes</h2>
 * The log remembers the zone of every card it has seen arrive somewhere ({@link #arrived}, {@link #entered},
 * {@link #track}), so an arrival names the zone the card came from even when the card was taken out of that
 * zone first. A move inside one zone (a library reordered, a card put back on top) is no zone change and
 * emits nothing.
 *
 * <p>A permanent leaving the battlefield is emitted as it leaves ({@link #departed}), with its last-known
 * snapshot and the zone it is meant to go to. If its card then arrives in another zone, because a replacement
 * effect sent it there (CR 614.6), the departure event is rewritten to name that zone instead, so one
 * departure is always one event. A token that ceases to exist keeps the zone it was put in (CR 111.7).
 *
 * <h2>Causes</h2>
 * The engine names why a particular object is moving ({@link #setCause}) around the call that moves it, so
 * that a sacrifice the engine redirects into exile is still a sacrifice. The cause applies only to that
 * object's own move.
 *
 * <h2>Suppression</h2>
 * Copying or restoring a game re-adds every card to its zone list. Those adds are not zone changes, so the
 * copy runs between {@link #suppress} and {@link #resume}, which emits and tracks nothing.
 */
public final class RulesEventLog {

    private static final class Batch {
        private final long id;
        private final List<RulesEvent> events = new ArrayList<>();

        private Batch(long id) {
            this.id = id;
        }

        private RulesEventBatch freeze() {
            return new RulesEventBatch(id, events);
        }
    }

    private final List<Batch> closed = new ArrayList<>();
    private Batch open;
    private int openDepth;
    private long nextBatchId = 1;
    private int suppressDepth;
    private final Map<UUID, Zone> cardZones = new HashMap<>();
    /** Departure events not yet drained, keyed by each physical card that left with the permanent. */
    private final Map<UUID, ZoneChangeEvent> departures = new HashMap<>();
    private final Map<UUID, ZoneChangeCause> causes = new HashMap<>();

    /** Opens a batch scope: until the matching {@link #endBatch}, every event joins one batch. */
    public void beginBatch() {
        if (openDepth++ == 0) {
            open = new Batch(nextBatchId++);
        }
    }

    /** Closes a batch scope; the outermost one closes the batch, which is dropped if it stayed empty. */
    public void endBatch() {
        if (openDepth == 0) {
            return;
        }
        if (--openDepth == 0) {
            if (!open.events.isEmpty()) {
                closed.add(open);
            }
            open = null;
        }
    }

    /** Whether a batch scope is open. */
    public boolean isBatchOpen() {
        return openDepth > 0;
    }

    /** Adds {@code event} to the open batch, or as a batch of its own when none is open. */
    public void emit(RulesEvent event) {
        if (suppressDepth > 0) {
            return;
        }
        if (open != null) {
            open.events.add(event);
            return;
        }
        Batch single = new Batch(nextBatchId++);
        single.events.add(event);
        closed.add(single);
    }

    /** Stops emitting and tracking until the matching {@link #resume}. */
    public void suppress() {
        suppressDepth++;
    }

    public void resume() {
        if (suppressDepth > 0) {
            suppressDepth--;
        }
    }

    public boolean isSuppressed() {
        return suppressDepth > 0;
    }

    /** Records that {@code card} is in {@code zone} without emitting anything, for cards a game starts with. */
    public void track(Card card, Zone zone) {
        if (suppressDepth == 0 && card != null) {
            cardZones.put(card.getId(), zone);
        }
    }

    /** The zone the log last saw the card arrive in, or {@code null}. */
    public Zone zoneOf(UUID cardId) {
        return cardZones.get(cardId);
    }

    /**
     * Names why object {@code objectId} (a permanent or a card) is about to move, until
     * {@link #restoreCause} puts {@code previous}, the value this returns, back.
     */
    public ZoneChangeCause setCause(UUID objectId, ZoneChangeCause cause) {
        return causes.put(objectId, cause);
    }

    public void restoreCause(UUID objectId, ZoneChangeCause previous) {
        if (previous == null) {
            causes.remove(objectId);
        } else {
            causes.put(objectId, previous);
        }
    }

    /**
     * {@code card} was added to {@code to}, the zone list of {@code ownerId}. Emits the zone change from the
     * zone the card was last seen in, or rewrites the departure of the permanent it just left the battlefield
     * as.
     */
    public void arrived(Card card, UUID ownerId, Zone to) {
        if (suppressDepth > 0 || card == null) {
            return;
        }
        UUID cardId = card.getId();
        if (consumeDeparture(cardId, to)) {
            cardZones.put(cardId, to);
            return;
        }
        Zone from = cardZones.put(cardId, to);
        if (from == to) {
            return;
        }
        emit(new ZoneChangeEvent(card, ownerId, from, to, causes.getOrDefault(cardId, ZoneChangeCause.OTHER),
                null, null));
    }

    /**
     * {@code permanent} entered the battlefield under {@code controllerId}'s control. Emits the zone change
     * from the zone its card was last seen in, with a snapshot of the permanent as it entered.
     */
    public void entered(Permanent permanent, UUID ownerId, UUID controllerId) {
        if (suppressDepth > 0) {
            return;
        }
        Card card = permanent.getOriginalCard();
        Zone from = cardZones.get(card.getId());
        if (from == null) {
            from = permanent.getEnteredFromZone();
        }
        for (UUID cardId : List.of(card.getId(), permanent.getCard().getId())) {
            consumeDeparture(cardId, null);
            cardZones.put(cardId, Zone.BATTLEFIELD);
        }
        emit(new ZoneChangeEvent(card, ownerId, from, Zone.BATTLEFIELD,
                causes.getOrDefault(permanent.getId(), ZoneChangeCause.OTHER),
                new Permanent(permanent), controllerId));
    }

    /**
     * Withdraws the entry event of permanent {@code permanentId}, whose entry a replacement turned into something
     * else after it was placed (an Upgrade permanent exiled instead, or merged into the artifact it covers). Its
     * card is back in the zone it came from, so its next arrival names that zone.
     */
    public void retractEntry(UUID permanentId) {
        if (suppressDepth > 0) {
            return;
        }
        List<Batch> batches = new ArrayList<>(closed);
        if (open != null) {
            batches.add(open);
        }
        for (Batch batch : batches) {
            for (int i = batch.events.size() - 1; i >= 0; i--) {
                if (batch.events.get(i) instanceof ZoneChangeEvent entry && entry.enteredBattlefield()
                        && entry.permanent().getId().equals(permanentId)) {
                    batch.events.remove(i);
                    if (batch.events.isEmpty() && batch != open) {
                        closed.remove(batch);
                    }
                    if (entry.from() == null) {
                        cardZones.remove(entry.card().getId());
                    } else {
                        cardZones.put(entry.card().getId(), entry.from());
                    }
                    return;
                }
            }
        }
    }

    /**
     * A permanent left the battlefield, meant for {@code intendedTo}.
     *
     * @param lastKnown    its last-known snapshot; the log keeps it, so pass a copy
     * @param card         its physical card (the original card of a merged or melded permanent)
     * @param cardIds      every card that leaves with it, whose arrival elsewhere rewrites the destination
     * @param ownerId      the card's owner
     * @param controllerId its controller as it left
     */
    public void departed(Permanent lastKnown, Card card, Collection<UUID> cardIds, UUID ownerId,
                         UUID controllerId, Zone intendedTo) {
        if (suppressDepth > 0) {
            return;
        }
        ZoneChangeEvent event = new ZoneChangeEvent(card, ownerId, Zone.BATTLEFIELD, intendedTo,
                causes.getOrDefault(lastKnown.getId(), ZoneChangeCause.OTHER), lastKnown, controllerId);
        for (UUID cardId : cardIds) {
            departures.put(cardId, event);
            cardZones.put(cardId, intendedTo);
        }
        emit(event);
    }

    /**
     * If {@code cardId} left the battlefield and hasn't arrived anywhere yet, ends that departure's wait:
     * with a non-null {@code to} that differs from its destination, the departure event is rewritten to it.
     */
    private boolean consumeDeparture(UUID cardId, Zone to) {
        ZoneChangeEvent departure = departures.get(cardId);
        if (departure == null) {
            return false;
        }
        departures.values().removeIf(waiting -> waiting == departure);
        if (to != null && to != departure.to()) {
            replace(departure, departure.withTo(to));
        }
        return true;
    }

    private void replace(RulesEvent previous, RulesEvent replacement) {
        List<Batch> batches = new ArrayList<>(closed);
        if (open != null) {
            batches.add(open);
        }
        for (Batch batch : batches) {
            for (int i = 0; i < batch.events.size(); i++) {
                if (batch.events.get(i) == previous) {
                    batch.events.set(i, replacement);
                    return;
                }
            }
        }
    }

    /** Every batch not yet drained, the open one included, without taking them. */
    public List<RulesEventBatch> pending() {
        List<RulesEventBatch> pending = new ArrayList<>(closed.size() + 1);
        closed.forEach(batch -> pending.add(batch.freeze()));
        if (open != null && !open.events.isEmpty()) {
            pending.add(open.freeze());
        }
        return pending;
    }

    /**
     * Takes every closed batch, in order. An open batch stays open, and a departure that hasn't arrived
     * anywhere by now keeps the destination it was emitted with.
     */
    public List<RulesEventBatch> drain() {
        List<RulesEventBatch> drained = new ArrayList<>(closed.size());
        closed.forEach(batch -> drained.add(batch.freeze()));
        closed.clear();
        if (open == null) {
            departures.clear();
        } else {
            departures.values().removeIf(waiting -> !open.events.contains(waiting));
        }
        return drained;
    }

    /** Replaces this log's state with a copy of {@code source}'s that shares no mutable snapshot. */
    public void copyFrom(RulesEventLog source) {
        Map<RulesEvent, RulesEvent> copies = new IdentityHashMap<>();
        closed.clear();
        for (Batch batch : source.closed) {
            closed.add(copyBatch(batch, copies));
        }
        open = source.open == null ? null : copyBatch(source.open, copies);
        openDepth = source.openDepth;
        nextBatchId = source.nextBatchId;
        cardZones.clear();
        cardZones.putAll(source.cardZones);
        causes.clear();
        causes.putAll(source.causes);
        departures.clear();
        source.departures.forEach((cardId, event) -> departures.put(cardId,
                (ZoneChangeEvent) copies.computeIfAbsent(event, RulesEvent::deepCopy)));
    }

    private static Batch copyBatch(Batch batch, Map<RulesEvent, RulesEvent> copies) {
        Batch copy = new Batch(batch.id);
        batch.events.forEach(event -> copy.events.add(copies.computeIfAbsent(event, RulesEvent::deepCopy)));
        return copy;
    }
}
