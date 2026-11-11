package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The record of a reflexive or delayed triggered ability an effect handler or a choice handler builds as another
 * stack object resolves ("when you do, ...", CR 603.12).
 *
 * <p>Such an ability is a stack object of its own, but what it means by "that creature", "that player", "that much"
 * and X is what the object that made it meant. {@link #record} builds the entry of the ability that chooses nothing
 * from the resolving entry; {@link #route} hands it to the game ({@link
 * GameData#handOffTriggerRecord}) while the handler takes its route, so a target, a graveyard card or a mode chosen
 * as the ability goes on the stack (a queued choice, a prompt, the entry a choice handler builds) carries the same
 * record ({@link StackEntry#carryTriggerRecord}). A handler never copies single fields of the resolving entry by
 * hand: what a reflexive ability reads is on the record.</p>
 */
public final class ReflexiveAbilitySupport {

    private ReflexiveAbilitySupport() {
    }

    /**
     * {@link #record(GameData, StackEntry, List, String, UUID)} named "[card]'s reflexive ability", from the resolving
     * entry's source.
     */
    public static StackEntry record(GameData gameData, StackEntry resolving, List<CardEffect> effects) {
        return record(gameData, resolving, effects, resolving.getCard().getName() + "'s reflexive ability");
    }

    /** {@link #record(GameData, StackEntry, List, String, UUID)} from the resolving entry's source. */
    public static StackEntry record(GameData gameData, StackEntry resolving, List<CardEffect> effects,
                                    String description) {
        return record(gameData, resolving, effects, description, resolving.getSourcePermanentId());
    }

    /**
     * The entry of a reflexive ability of {@code resolving} that chooses nothing: its card and controller, {@code
     * sourcePermanentId} as its source, the resolving entry's X, trigger event, source snapshot, active player and
     * alternative cost, and the player the resolving entry was about as its non-targeting "that player" (an entry a
     * choice builds with a target of its own keeps that target, {@link StackEntry#carryTriggerRecord}).
     */
    public static StackEntry record(GameData gameData, StackEntry resolving, List<CardEffect> effects,
                                    String description, UUID sourcePermanentId) {
        StackEntry record = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                resolving.getCard(),
                resolving.getControllerId(),
                description,
                new ArrayList<>(effects),
                resolving.getXValue(),
                sourcePermanentId);
        record.copyTriggerEventFrom(resolving);
        // The limit a graveyard target of the resolving entry was chosen against is that target's.
        record.setGraveyardTargetMaxManaValue(null);
        Permanent sourceSnapshot = resolving.getSourcePermanentSnapshot();
        if (sourceSnapshot != null && Objects.equals(sourcePermanentId, resolving.getSourcePermanentId())) {
            record.setSourcePermanentSnapshot(sourceSnapshot);
        }
        record.setActivePlayerId(resolving.getActivePlayerId());
        record.setAlternateCost(resolving.isAlternateCost());
        if (resolving.getTargetId() != null && gameData.playerIds.contains(resolving.getTargetId())) {
            record.setTargetId(resolving.getTargetId());
            record.setNonTargeting(true);
        }
        return record;
    }

    /**
     * Runs {@code routing}, a handler putting the reflexive ability {@code record} is the plain entry of toward the
     * stack, with the record handed off: the choice the ability is queued or asked with, or the entry that reaches
     * the stack for it, takes it. A record no route took (no legal target, CR 603.3d) is dropped.
     */
    public static void route(GameData gameData, StackEntry record, Runnable routing) {
        StackEntry previous = handOff(gameData, record);
        try {
            routing.run();
        } finally {
            gameData.restoreTriggerRecordInHand(previous);
        }
    }

    /** {@link #route(GameData, StackEntry, Runnable)} for a routing that returns a result. */
    public static <T> T route(GameData gameData, StackEntry record, Supplier<T> routing) {
        StackEntry previous = handOff(gameData, record);
        try {
            return routing.get();
        } finally {
            gameData.restoreTriggerRecordInHand(previous);
        }
    }

    /**
     * Hands {@code record} off. The entry that takes it is given its active player, so a record made as another
     * object resolves names the player whose turn it is, as an entry a choice builds does.
     */
    private static StackEntry handOff(GameData gameData, StackEntry record) {
        if (record.getActivePlayerId() == null) {
            record.setActivePlayerId(gameData.activePlayerId);
        }
        return gameData.handOffTriggerRecord(record);
    }
}
