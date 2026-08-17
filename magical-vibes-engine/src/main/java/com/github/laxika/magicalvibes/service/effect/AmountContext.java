package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.List;
import java.util.UUID;

/**
 * Evaluation context for a {@link com.github.laxika.magicalvibes.model.amount.DynamicAmount}
 * (the numeric sibling of {@link ConditionContext}). Carries the source-of-truth values that
 * differ between evaluation sites so that {@link AmountEvaluationService} can host a single
 * exhaustive switch.
 *
 * @param controllerId      controller of the spell/ability/permanent the amount belongs to
 * @param sourcePermanent   the source permanent, when one exists (source-relative amounts
 *                          such as attachments or blockers evaluate to 0 without it)
 * @param targetPermanentId id of the resolved target permanent, when resolving a targeted effect —
 *                          read from the stack entry the same way {@code ConditionContext.targetId}
 *                          is; target-relative amounts ({@code TargetToughness}) look it up here and
 *                          evaluate to 0 without a legal target
 * @param xValue            snapshotted cast-time context from the stack entry (mana spent, X paid)
 * @param eventValue        snapshotted trigger-event / prior-resolution context from the stack entry
 *                          (life gained, damage dealt, excess damage); read by {@code EventValue}
 * @param staticEvaluation  whether this evaluation is for a static continuous effect
 * @param chosenPermanentId id of the permanent chosen while activating the ability behind this
 *                          entry or during a library selection; read by {@code ChosenPermanentPower}.
 *                          {@code null} outside stack resolution
 * @param repeatedAdditionalCosts the mana payments chosen for a
 *                          {@code RepeatableAdditionalManaCost} as the spell was cast, one entry
 *                          per repetition; read by {@code RepeatedAdditionalCostCount}. Empty
 *                          outside stack resolution
 * @param sourceCard        the card behind the spell or ability, when one exists. Read by
 *                          {@code SourceCardPower} for abilities that have no source permanent at
 *                          all (scavenge activates from the graveyard). {@code null} outside stack
 *                          resolution
 * @param chosenPermanentPowerAtTrigger last-known effective power captured for an entering permanent
 *                          carried as the chosen permanent; used if that permanent leaves before
 *                          resolution
 * @param triggeringPermanentPowerAtTrigger effective power captured for the permanent that caused
 *                          a trigger, used as last-known information when an enchanted permanent
 *                          leaves before resolution
 * @param sacrificedPower   effective power snapshotted from a permanent sacrificed as a cost
 * @param sacrificedToughness effective toughness snapshotted from a permanent sacrificed as a cost
 */
public record AmountContext(
        UUID controllerId,
        Permanent sourcePermanent,
        UUID targetPermanentId,
        int xValue,
        int eventValue,
        boolean staticEvaluation,
        UUID chosenPermanentId,
        List<String> repeatedAdditionalCosts,
        Card sourceCard,
        Integer chosenPermanentPowerAtTrigger,
        Integer triggeringPermanentPowerAtTrigger,
        int sacrificedPower,
        int sacrificedToughness
) {

    /** Backward-compatible context constructor without last-known or sacrificed-permanent snapshots. */
    public AmountContext(UUID controllerId, Permanent sourcePermanent, UUID targetPermanentId,
                         int xValue, int eventValue, boolean staticEvaluation,
                         UUID chosenPermanentId, List<String> repeatedAdditionalCosts, Card sourceCard) {
        this(controllerId, sourcePermanent, targetPermanentId, xValue, eventValue, staticEvaluation,
                chosenPermanentId, repeatedAdditionalCosts, sourceCard, null, null, 0, 0);
    }

    /** Convenience for the common case with no repeatable additional cost payments. */
    public AmountContext(UUID controllerId, Permanent sourcePermanent, UUID targetPermanentId,
                         int xValue, int eventValue, UUID chosenPermanentId) {
        this(controllerId, sourcePermanent, targetPermanentId, xValue, eventValue, false,
                chosenPermanentId, List.of(), null);
    }

    /** Convenience for the common case with no chosen permanent (all non-stack-resolution sites). */
    public AmountContext(UUID controllerId, Permanent sourcePermanent, UUID targetPermanentId,
                         int xValue, int eventValue) {
        this(controllerId, sourcePermanent, targetPermanentId, xValue, eventValue, null);
    }

    /** Convenience for sites that know the chosen permanent but have no separate source card. */
    public AmountContext(UUID controllerId, Permanent sourcePermanent, UUID targetPermanentId,
                         int xValue, int eventValue, boolean staticEvaluation, UUID chosenPermanentId) {
        this(controllerId, sourcePermanent, targetPermanentId, xValue, eventValue, staticEvaluation,
                chosenPermanentId, List.of(), null);
    }

    /**
     * The same context re-pointed at another player, for effects that evaluate one amount once per
     * player ("each player's life total becomes the number of creatures <em>they</em> control").
     * Player-relative scopes ({@code CountScope.CONTROLLER}) then read that player.
     */
    public AmountContext withControllerId(UUID otherControllerId) {
        return new AmountContext(otherControllerId, sourcePermanent, targetPermanentId, xValue,
                eventValue, staticEvaluation, chosenPermanentId, repeatedAdditionalCosts, sourceCard,
                chosenPermanentPowerAtTrigger, triggeringPermanentPowerAtTrigger,
                sacrificedPower, sacrificedToughness);
    }

    /** Context for resolving an effect on a stack entry (stack resolution time). */
    public static AmountContext forStackEntry(StackEntry entry, Permanent sourcePermanent) {
        return new AmountContext(entry.getControllerId(), sourcePermanent, entry.getTargetId(),
                entry.getXValue(), entry.getEventValue(), false, entry.getChosenPermanentId(),
                entry.getRepeatedAdditionalCosts(), entry.getCard(), entry.getTriggeringPermanentPowerAtTrigger(),
                entry.getTriggeringPermanentPowerAtTrigger(),
                entry.getSacrificedPower(),
                entry.getSacrificedToughness());
    }

    /**
     * Context for static (continuous) effect computation from a source permanent. It no longer
     * selects the recursion-safe matchers — {@code GameQueryService.isStaticEvaluationActive()}
     * observes whether an assembly or a board build is in flight — so this is now a plain
     * source-and-controller context, kept for what it says at the call site.
     */
    public static AmountContext forStaticEffect(Permanent source, UUID controllerId) {
        return new AmountContext(controllerId, source, null, 0, 0, true, null, List.of(), null);
    }

    /**
     * Context for a mana ability resolving outside the stack (no priority round, no {@code xValue}).
     * Mana abilities are resolved immediately from the source permanent and its controller
     * (CR 605.3a), so there is no {@link StackEntry} to read.
     */
    public static AmountContext forManaAbility(Permanent source, UUID controllerId) {
        return forManaAbility(source, controllerId, 0);
    }

    /**
     * Mana-ability context that carries the activation-time {@code xValue}, for abilities whose
     * cost snapshots a number the mana amount scales off (Soldevi Adnate's sacrificed mana value).
     */
    public static AmountContext forManaAbility(Permanent source, UUID controllerId, int xValue) {
        return new AmountContext(controllerId, source, null, xValue, 0);
    }

    /** Context for evaluating replacement effects on a permanent as it enters the battlefield. */
    public static AmountContext forEnteringPermanent(UUID controllerId, Permanent permanent, int xValue) {
        return new AmountContext(controllerId, permanent, null, xValue, 0, false, null,
                List.of(), permanent.getCard());
    }

    /** Source-less context for heuristic estimation (AI evaluation). */
    public static AmountContext forEstimation(UUID controllerId) {
        return new AmountContext(controllerId, null, null, 0, 0);
    }

    /**
     * Context for computing a spell's cast cost, before any stack entry exists. A spell being
     * cast from hand has no source permanent; only player-relative counting amounts
     * (graveyard/battlefield counts) are meaningful here.
     */
    public static AmountContext forCasting(UUID castingPlayerId) {
        return forCasting(castingPlayerId, 0);
    }

    /**
     * Cast-time context that also carries the announced X, for amounts that depend on it before a
     * stack entry exists (Meteor Shower — "X plus 1 damage divided as you choose").
     */
    public static AmountContext forCasting(UUID castingPlayerId, int xValue) {
        return new AmountContext(castingPlayerId, null, null, xValue, 0);
    }

    /** Cast-time context for a spell whose source card is still in a zone being counted. */
    public static AmountContext forCasting(UUID castingPlayerId, int xValue, Card sourceCard) {
        return new AmountContext(castingPlayerId, null, null, xValue, 0, false, null,
                List.of(), sourceCard);
    }
}
