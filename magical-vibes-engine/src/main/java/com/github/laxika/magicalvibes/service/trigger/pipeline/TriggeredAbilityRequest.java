package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.trigger.TriggerContext;

import java.util.Objects;
import java.util.UUID;

/**
 * A triggered ability whose trigger event has occurred, as its collector hands it to the stack door
 * ({@link TriggeredAbilityStackService#put}): the ability whole, its source and controller, and the record of the
 * event. The collector has judged the trigger's filters and event condition; the door does everything else.
 *
 * @param ability          the ability, with its options and its effects as the card wrote them
 * @param sourceCard       the card whose ability it is
 * @param sourcePermanent  the permanent whose ability it is (CR 113.7), or {@code null} for an ability of a card in
 *                         another zone
 * @param controllerId     the ability's controller (CR 113.8)
 * @param description      what the stack entry is called
 * @param ifContext        the context the intervening "if" is judged in as the ability triggers (CR 603.4)
 * @param record           stamps what the event was on the stack entry
 * @param attack           whether creatures attacking caused the ability to trigger, which the "triggers an
 *                         additional time" effects that count only those read, and whether its source was one of them
 * @param event            the event as the "triggers an additional time" effects that judge it read it (a combat
 *                         damage event's source and the damage it dealt to players), or {@code null}
 * @param limitCreatureId  the creature the event names, which "triggers only once each turn for each creature" judges
 *                         and marks the ability's limit by, or {@code null}
 * @param creatureDying    whether a creature dying caused the ability to trigger, which "if a creature dying causes a
 *                         triggered ability to trigger, it triggers an additional time" effects count (Teysa Karlov):
 *                         not for an ability that triggers on the sacrifice that made the creature die
 */
public record TriggeredAbilityRequest(TriggeredAbility ability, Card sourceCard, Permanent sourcePermanent,
                                      UUID controllerId, String description, ConditionContext ifContext,
                                      TriggerRecord record, AttackCause attack, TriggerContext event,
                                      UUID limitCreatureId, boolean creatureDying) {

    public TriggeredAbilityRequest {
        Objects.requireNonNull(ability, "ability");
        Objects.requireNonNull(sourceCard, "sourceCard");
        Objects.requireNonNull(controllerId, "controllerId");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(ifContext, "ifContext");
        Objects.requireNonNull(record, "record");
    }

    /** A request for an ability that no attack caused, read by the doublers without its event. */
    public TriggeredAbilityRequest(TriggeredAbility ability, Card sourceCard, Permanent sourcePermanent,
                                   UUID controllerId, String description, ConditionContext ifContext,
                                   TriggerRecord record) {
        this(ability, sourceCard, sourcePermanent, controllerId, description, ifContext, record, AttackCause.NONE,
                null, null, false);
    }

    /** This request for an ability creatures attacking caused to trigger, its source not among them. */
    public TriggeredAbilityRequest causedByAttack() {
        return new TriggeredAbilityRequest(ability, sourceCard, sourcePermanent, controllerId, description, ifContext,
                record, AttackCause.ATTACK, event, limitCreatureId, creatureDying);
    }

    /**
     * This request for an ability of a creature that its own attacking caused to trigger ("whenever this creature
     * attacks", "whenever you attack" of an attacker), which "a creature you control attacking causes a triggered
     * ability of that creature to trigger" watches (Firebender Ascension).
     */
    public TriggeredAbilityRequest causedBySourceAttacking() {
        return new TriggeredAbilityRequest(ability, sourceCard, sourcePermanent, controllerId, description, ifContext,
                record, AttackCause.SOURCE_ATTACKED, event, limitCreatureId, creatureDying);
    }

    /** This request with {@code event} as the event the "triggers an additional time" effects judge. */
    public TriggeredAbilityRequest withEvent(TriggerContext event) {
        return new TriggeredAbilityRequest(ability, sourceCard, sourcePermanent, controllerId, description, ifContext,
                record, attack, event, limitCreatureId, creatureDying);
    }

    /**
     * This request for an event about the creature {@code creatureId}, which "triggers only once each turn for each
     * creature" judges and marks the ability's limit by (Stalwart Successor).
     */
    public TriggeredAbilityRequest withLimitCreature(UUID creatureId) {
        return new TriggeredAbilityRequest(ability, sourceCard, sourcePermanent, controllerId, description, ifContext,
                record, attack, event, creatureId, creatureDying);
    }

    /** This request for an ability a creature dying caused to trigger (other than by being sacrificed). */
    public TriggeredAbilityRequest withCreatureDying() {
        return new TriggeredAbilityRequest(ability, sourceCard, sourcePermanent, controllerId, description, ifContext,
                record, attack, event, limitCreatureId, true);
    }

    /** Whether creatures attacking caused the ability to trigger (CR 508.3a). */
    public boolean attackCaused() {
        return attack != AttackCause.NONE;
    }

    /** Whether creatures attacking caused the ability to trigger, and whether its source was one of them. */
    public enum AttackCause {
        /** No attack caused it. */
        NONE,
        /** Creatures attacking caused it; its source isn't one of them. */
        ATTACK,
        /** Its source attacking caused it. */
        SOURCE_ATTACKED
    }

    /** The id of {@link #sourcePermanent}, or {@code null}. */
    public UUID sourcePermanentId() {
        return sourcePermanent == null ? null : sourcePermanent.getId();
    }

    /**
     * What a collector knows of the event: the objects and players it is about and the values it carries, stamped on
     * the ability's stack entry whatever the ability's effects are. A non-targeting {@code targetId} the record sets is
     * the object the event names ("that creature", CR 115.10a).
     */
    @FunctionalInterface
    public interface TriggerRecord {

        /** Records nothing beyond the source and controller. */
        TriggerRecord NONE = entry -> entry.setNonTargeting(true);

        /** Stamps the event on {@code entry}. */
        void stamp(StackEntry entry);
    }
}
