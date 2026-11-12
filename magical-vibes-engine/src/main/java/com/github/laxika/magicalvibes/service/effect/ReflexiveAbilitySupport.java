package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
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
 *
 * <p>A choice handler that builds such an ability once the resolving entry's question is answered has no entry in its
 * hands: {@link #resolvingEntryOf} finds the entry still resolving, and {@link #recordFor} builds the record from it
 * for the source card and controller the handler names. The accepted option of a question (a {@link
 * PendingMayAbility}) that is carried out in the resolution or the event that asked it resolves as an entry that
 * records what the question kept ({@link #mayAbilityRecord}), so a "when you do" among its steps is on the same
 * record.</p>
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
        recordResolving(gameData, record, resolving);
        return record;
    }

    private static void recordResolving(GameData gameData, StackEntry record, StackEntry resolving) {
        record.copyTriggerEventFrom(resolving);
        // The limit a graveyard target of the resolving entry was chosen against is that target's.
        record.setGraveyardTargetMaxManaValue(null);
        Permanent sourceSnapshot = resolving.getSourcePermanentSnapshot();
        if (sourceSnapshot != null
                && Objects.equals(record.getSourcePermanentId(), resolving.getSourcePermanentId())) {
            record.setSourcePermanentSnapshot(sourceSnapshot);
        }
        record.setActivePlayerId(resolving.getActivePlayerId());
        record.setAlternateCost(resolving.isAlternateCost());
        if (resolving.getTargetId() != null && gameData.playerIds.contains(resolving.getTargetId())) {
            record.setTargetId(resolving.getTargetId());
            record.setNonTargeting(true);
        }
    }

    /**
     * The entry whose resolution is waiting for an answer ({@link GameData#pendingEffectResolutionEntry}) when it is
     * an entry of {@code sourceCard} or of the permanent {@code sourcePermanentId}, else {@code null}: the entry a
     * choice handler's follow-up ability is a reflexive ability of.
     */
    public static StackEntry resolvingEntryOf(GameData gameData, Card sourceCard, UUID sourcePermanentId) {
        StackEntry resolving = gameData.pendingEffectResolutionEntry;
        if (resolving == null) {
            return null;
        }
        Card resolvingCard = resolving.getCard();
        boolean sameCard = resolvingCard == sourceCard || resolvingCard != null && sourceCard != null
                && resolvingCard.getId() != null && resolvingCard.getId().equals(sourceCard.getId());
        boolean samePermanent = sourcePermanentId != null
                && sourcePermanentId.equals(resolving.getSourcePermanentId());
        return sameCard || samePermanent ? resolving : null;
    }

    /**
     * {@link #record(GameData, StackEntry, List, String, UUID)} for an ability whose handler names its source card
     * and controller itself (a choice handler's follow-up, the payer of a cost who isn't the source's controller):
     * the same record of {@code resolving} with {@code sourceCard} and {@code controllerId}, or, with no resolving
     * entry, an entry that records no event.
     */
    public static StackEntry recordFor(GameData gameData, StackEntry resolving, Card sourceCard, UUID controllerId,
                                       List<CardEffect> effects, String description, UUID sourcePermanentId) {
        StackEntry record = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                description,
                new ArrayList<>(effects),
                resolving == null ? 0 : resolving.getXValue(),
                sourcePermanentId);
        if (resolving != null) {
            recordResolving(gameData, record, resolving);
        }
        return record;
    }

    /**
     * The entry the accepted option of a question resolves as, never put on the stack itself (CR 608.2d; a triggered
     * ability's own "you may" is asked as its entry resolves, CR 603.5): the question's player as its controller,
     * what the question kept of the trigger event (the event's value, player, card, spell and permanent, the source
     * as it was), the object or player the question named as its non-targeting target, and {@code xValuePaid} as
     * its X when it has no permanent source and the question recorded no X.
     */
    public static StackEntry mayAbilityRecord(PendingMayAbility ability, List<CardEffect> effects,
                                              String description, UUID sourcePermanentId, int xValuePaid) {
        return mayAbilityRecord(ability, ability.sourceCard(), effects, description, sourcePermanentId, xValuePaid);
    }

    /**
     * {@link #mayAbilityRecord(PendingMayAbility, List, String, UUID, int)} with {@code sourceCard} as the ability's
     * source: the card of the source permanent as it is now, when the question's card is the one it was asked with.
     */
    public static StackEntry mayAbilityRecord(PendingMayAbility ability, Card sourceCard, List<CardEffect> effects,
                                              String description, UUID sourcePermanentId, int xValuePaid) {
        StackEntry record = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                ability.controllerId(),
                description,
                new ArrayList<>(effects),
                ability.targetCardId(),
                sourcePermanentId);
        if (ability.sourcePermanentId() == null) {
            record.setXValue(xValuePaid);
        }
        if (ability.targetCardId() != null) {
            record.setNonTargeting(true);
        }
        record.setActivePlayerId(ability.activePlayerId());
        record.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        record.setEventValue(ability.eventValue());
        record.setTriggeringPermanentId(ability.triggeringPermanentId());
        record.setGrantingPermanentId(ability.grantingPermanentId());
        record.setTriggeringSpell(ability.triggeringSpellSnapshot());
        if (ability.sourceControllerId() != null) {
            record.setTriggeringPermanentControllerId(ability.sourceControllerId());
        }
        record.setTriggeringPermanentPowerAtTrigger(ability.sourcePowerAtTrigger());
        record.setTriggeringPermanentToughnessAtTrigger(ability.triggeringPermanentToughnessAtTrigger());
        record.setTriggeringCardId(ability.triggeringCardId());
        record.setTriggeringCardGraveyardEntryVersion(ability.triggeringCardGraveyardEntryVersion());
        record.setSourceGraveyardEntryVersion(ability.sourceGraveyardEntryVersion());
        if (ability.xValue() != null) {
            record.setXValue(ability.xValue());
        }
        record.setAttackedTargetId(ability.attackedTargetId());
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
