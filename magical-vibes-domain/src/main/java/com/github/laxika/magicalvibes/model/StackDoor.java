package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The only way to build a triggered ability's stack entry and put it toward the stack. Its members are
 * {@code protected}: only its subclass the stack door ({@code TriggeredAbilityStackService}) reaches them, and a test
 * fixture's door that puts hand-built abilities on a test's stack. {@link StackEntry}'s public constructors refuse
 * {@link StackEntryType#TRIGGERED_ABILITY}, and {@link GameData#enqueueTrigger} and
 * {@link GameData#handOffTriggerRecord} are package-private. The stack ({@link GameData#stack}) refuses a
 * triggered entry the door has not released, so an entry the door built but did not route (an ability a resolution
 * made, before {@code putMade}; an ability parked for a choice) can't reach it either. Copies of a stack object
 * ({@link StackEntry#copyForNewStackObject()}, the copy constructor) keep what they copy.
 */
public abstract class StackDoor {

    protected StackDoor() {
    }

    /**
     * A new triggered ability's stack entry of {@code sourceCard}, controlled by {@code controllerId}, resolving
     * {@code effects}, not yet released to the stack.
     */
    protected static StackEntry newTriggeredEntry(Card sourceCard, UUID controllerId, String description,
                                                  List<CardEffect> effects, int xValue, UUID targetId,
                                                  UUID sourcePermanentId, Zone targetZone,
                                                  List<UUID> targetCardIds, List<UUID> targetIds) {
        return newTriggeredEntry(sourceCard, controllerId, description, effects, xValue, targetId, sourcePermanentId,
                null, targetZone, targetCardIds, targetIds);
    }

    /** {@link #newTriggeredEntry(Card, UUID, String, List, int, UUID, UUID, Zone, List, List)} with a division. */
    protected static StackEntry newTriggeredEntry(Card sourceCard, UUID controllerId, String description,
                                                  List<CardEffect> effects, int xValue, UUID targetId,
                                                  UUID sourcePermanentId, Map<UUID, Integer> damageAssignments,
                                                  Zone targetZone, List<UUID> targetCardIds, List<UUID> targetIds) {
        return StackEntry.triggered(sourceCard, controllerId, description, effects, xValue, targetId,
                sourcePermanentId, damageAssignments, targetZone, targetCardIds, targetIds);
    }

    /**
     * {@code record}, a triggered ability's entry the door parked for a choice, as a new stack object whose card is
     * {@code card} (a runtime copy of the record's card that declares the targets of the modes chosen), with everything
     * else the record holds.
     */
    protected static StackEntry parkedOn(StackEntry record, Card card) {
        return record.copyForNewStackObject(card);
    }

    /**
     * Releases {@code entry} and puts it toward the stack ({@link GameData#enqueueTrigger}): on the stack, or held while
     * a mana ability resolves or a subgame runs.
     */
    protected static void enqueue(GameData gameData, StackEntry entry) {
        entry.releaseToStack();
        gameData.enqueueTrigger(entry);
    }

    /**
     * Releases {@code entry} to the stack without putting it there, for a door that places it itself (a test fixture
     * that builds a game's stack by hand).
     */
    protected static StackEntry released(StackEntry entry) {
        entry.releaseToStack();
        return entry;
    }

    /** {@link GameData#handOffTriggerRecord}: hands off the record of the ability being routed. */
    protected static StackEntry handOff(GameData gameData, StackEntry record) {
        return gameData.handOffTriggerRecord(record);
    }
}
