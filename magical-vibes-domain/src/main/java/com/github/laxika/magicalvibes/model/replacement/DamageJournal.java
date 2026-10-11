package com.github.laxika.magicalvibes.model.replacement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The damage events one resolving effect started, in order, kept while one of them waits for its
 * player to order replacement effects (CR 616.1). The effect is then resolved again: events already
 * dealt report their result without being dealt twice, the one whose order was chosen is dealt at
 * its place, and the ones held back behind it are dealt as usual. That keeps a handler that reads
 * the damage it dealt ("destroy it if it was dealt damage this way") correct across the prompt.
 */
public final class DamageJournal {

    /** What one damage event of the effect has become. */
    public sealed interface Slot {

        /** Dealt; {@code dealt} is what the event reported. */
        record Done(int dealt) implements Slot {}

        /** Its player is still ordering replacement effects. */
        record Awaiting() implements Slot {}

        /** Every replacement has been applied; it is dealt when the effect resolves again. */
        record Ready(DamageEvent event) implements Slot {}

        /** Not begun because an earlier event of the effect was waiting; begins on the next pass. */
        record Held() implements Slot {}
    }

    private final UUID entryId;
    private final int effectIndex;
    private final List<Slot> slots;
    private int cursor;
    private boolean blocked;
    private boolean active;
    private boolean rerunRequested;
    private int depth;

    public DamageJournal(UUID entryId, int effectIndex) {
        this(entryId, effectIndex, new ArrayList<>(), 0, false, false, false);
    }

    private DamageJournal(UUID entryId, int effectIndex, List<Slot> slots, int cursor, boolean blocked,
                          boolean active, boolean rerunRequested) {
        this.entryId = entryId;
        this.effectIndex = effectIndex;
        this.slots = slots;
        this.cursor = cursor;
        this.blocked = blocked;
        this.active = active;
        this.rerunRequested = rerunRequested;
    }

    public boolean isFor(UUID entryId, int effectIndex) {
        return this.entryId.equals(entryId) && this.effectIndex == effectIndex;
    }

    /** Starts a pass over the effect; a later pass replays the slots from the beginning. */
    public void beginPass() {
        cursor = 0;
        blocked = false;
        active = true;
    }

    public void endPass() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void block() {
        blocked = true;
    }

    /** The slot the next damage event of this pass fills, or {@code null} when it is a new one. */
    public Slot nextSlot() {
        return cursor < slots.size() ? slots.get(cursor) : null;
    }

    /** Claims the next slot for an event of this pass and returns its index. */
    public int claim(Slot slot) {
        int index = cursor++;
        if (index < slots.size()) {
            slots.set(index, slot);
        } else {
            slots.add(slot);
        }
        return index;
    }

    public void set(int index, Slot slot) {
        slots.set(index, slot);
    }

    public Slot get(int index) {
        return index >= 0 && index < slots.size() ? slots.get(index) : null;
    }

    /** Whether an event of the effect still has to be dealt. */
    public boolean hasUnfinishedEvents() {
        return slots.stream().anyMatch(slot -> !(slot instanceof Slot.Done));
    }

    /** Damage being processed while the effect resolves; damage it causes isn't the effect's own. */
    public void enterDamage() {
        depth++;
    }

    public void exitDamage() {
        depth--;
    }

    public boolean isProcessingDamage() {
        return depth > 0;
    }

    public boolean isRerunRequested() {
        return rerunRequested;
    }

    public void setRerunRequested(boolean rerunRequested) {
        this.rerunRequested = rerunRequested;
    }

    public DamageJournal copyForSimulation() {
        List<Slot> copiedSlots = new ArrayList<>(slots.size());
        for (Slot slot : slots) {
            copiedSlots.add(slot instanceof Slot.Ready ready ? new Slot.Ready(ready.event().copyForSimulation()) : slot);
        }
        return new DamageJournal(entryId, effectIndex, copiedSlots, cursor, blocked, active, rerunRequested);
    }
}
