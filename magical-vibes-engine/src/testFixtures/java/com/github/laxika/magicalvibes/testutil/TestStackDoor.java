package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackDoor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A test's stack door: builds triggered abilities' stack entries by hand for a test that sets up a game's stack (or
 * resolves an entry) itself, released so the stack takes them. Each {@code triggered} factory takes the arguments of a
 * {@link StackEntry} constructor without its entry type; the engine builds them only through its stack door.
 */
public final class TestStackDoor extends StackDoor {

    private TestStackDoor() {
    }

    public static StackEntry triggered(Card card, UUID controllerId, String description, List<CardEffect> effects) {
        return entry(card, controllerId, description, effects, 0, null, null, null, null, null, null);
    }

    public static StackEntry triggered(Card card, UUID controllerId, String description, List<CardEffect> effects,
                                       int xValue) {
        return entry(card, controllerId, description, effects, xValue, null, null, null, null, null, null);
    }

    public static StackEntry triggered(Card card, UUID controllerId, String description, List<CardEffect> effects,
                                       int xValue, UUID targetId, Map<UUID, Integer> damageAssignments) {
        return entry(card, controllerId, description, effects, xValue, targetId, null, damageAssignments, null, null,
                null);
    }

    public static StackEntry triggered(Card card, UUID controllerId, String description, List<CardEffect> effects,
                                       int xValue, UUID sourcePermanentId) {
        return entry(card, controllerId, description, effects, xValue, null, sourcePermanentId, null, null, null, null);
    }

    public static StackEntry triggered(Card card, UUID controllerId, String description, List<CardEffect> effects,
                                       UUID targetId, UUID sourcePermanentId) {
        return entry(card, controllerId, description, effects, 0, targetId, sourcePermanentId, null, null, null, null);
    }

    public static StackEntry triggered(Card card, UUID controllerId, String description, List<CardEffect> effects,
                                       UUID targetId, Zone targetZone) {
        return entry(card, controllerId, description, effects, 0, targetId, null, null, targetZone, null, null);
    }

    public static StackEntry triggered(Card card, UUID controllerId, String description, List<CardEffect> effects,
                                       UUID targetId, Zone targetZone, UUID sourcePermanentId) {
        return entry(card, controllerId, description, effects, 0, targetId, sourcePermanentId, null, targetZone, null,
                null);
    }

    public static StackEntry triggered(Card card, UUID controllerId, String description, List<CardEffect> effects,
                                       int xValue, UUID targetId, UUID sourcePermanentId,
                                       Map<UUID, Integer> damageAssignments, Zone targetZone,
                                       List<UUID> targetCardIds, List<UUID> targetIds) {
        return entry(card, controllerId, description, effects, xValue, targetId, sourcePermanentId, damageAssignments,
                targetZone, targetCardIds, targetIds);
    }

    public static StackEntry triggered(Card card, UUID controllerId, String description, List<CardEffect> effects,
                                       List<UUID> targetCardIds) {
        return entry(card, controllerId, description, effects, 0, null, null, null, null, targetCardIds, null);
    }

    public static StackEntry triggered(Card card, UUID controllerId, String description, List<CardEffect> effects,
                                       UUID sourcePermanentId, List<UUID> targetIds) {
        return entry(card, controllerId, description, effects, 0, null, sourcePermanentId, null, null, null, targetIds);
    }

    public static StackEntry triggered(Card card, UUID controllerId, String description, List<CardEffect> effects,
                                       int xValue, List<UUID> targetIds) {
        return entry(card, controllerId, description, effects, xValue, null, null, null, null, null, targetIds);
    }

    /** Puts {@code entry}, a triggered ability's, toward {@code gameData}'s stack as the engine's door does. */
    public static void enqueue(GameData gameData, StackEntry entry) {
        StackDoor.enqueue(gameData, entry);
    }

    /** Hands off {@code record} as the engine's door does for the ability it routes; returns what was in hand. */
    public static StackEntry handOff(GameData gameData, StackEntry record) {
        return StackDoor.handOff(gameData, record);
    }

    private static StackEntry entry(Card card, UUID controllerId, String description, List<CardEffect> effects,
                                    int xValue, UUID targetId, UUID sourcePermanentId,
                                    Map<UUID, Integer> damageAssignments, Zone targetZone,
                                    List<UUID> targetCardIds, List<UUID> targetIds) {
        return released(newTriggeredEntry(card, controllerId, description, effects, xValue, targetId,
                sourcePermanentId, damageAssignments, targetZone, targetCardIds, targetIds));
    }
}
