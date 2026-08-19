package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.UUID;

/**
 * Carry-over work attached to a {@link PendingInteraction.DiscardChoice} and performed (or
 * continued) when the discard sequence completes. Rides the fresh-record-per-pick re-begins,
 * replacing the per-mechanic {@code pending*} fields {@code GameData} used to hold:
 * {@code rummageDrawCount} draws that many cards afterwards ("discard, then draw");
 * {@code untapPermanentId} untaps the source afterwards ("discard a card, then untap [source]");
 * {@code boostPermanentId}/{@code boostPower}/{@code boostToughness} pumps the source afterwards
 * ("discard a card, then this creature gets +X/+Y until end of turn");
 * {@code graveyardReturnCount} returns that many cards from the controller's graveyard to hand
 * afterwards ("discard X cards, then return a card for each discarded", Recall);
 * the each-player trio is the APNAP remainder of an "each player discards" flow, advanced by
 * {@code PlayerInteractionSupport.startNextEachPlayerDiscard} after each player finishes.
 * {@code eachPlayerAmounts} (when non-empty) overrides the shared {@code eachPlayerAmount} with a
 * per-chooser amount parallel to {@code remainingEachPlayerDiscards}, so different players can
 * discard different counts ("each player discards a third of their hand", Pox).
 * {@code plusOnePlusOneCounterPermanentId}/{@code plusOnePlusOneCounterAmount} put counters on a
 * source permanent after a filtered discard completes.
 * {@code thenEffect}/{@code thenEffectSourceCard} push {@code thenEffect} as a reflexive triggered
 * ability afterwards ("discard a [matching] card. If you do, [effect]", Pack Guardian).
 * {@code thenEffectTargetId} preserves a pre-bound permanent reference for that reflexive effect.
 * {@code thenEffectUsesDiscardedManaValue} supplies the discarded card's mana value as X while
 * choosing and resolving a targeted reflexive effect.
 */
public record DiscardFollowUp(int rummageDrawCount, UUID untapPermanentId,
                              List<UUID> remainingEachPlayerDiscards,
                              UUID eachPlayerControllerId, int eachPlayerAmount,
                              int graveyardReturnCount, List<Integer> eachPlayerAmounts,
                              UUID boostPermanentId, int boostPower, int boostToughness,
                              Card thenEffectSourceCard, CardEffect thenEffect, CardPredicate thenEffectCondition,
                              Permanent enteringPermanent, UUID enteringControllerId,
                              UUID plusOnePlusOneCounterPermanentId, int plusOnePlusOneCounterAmount,
                              UUID thenEffectTargetId, boolean plaguecrafter,
                              int eachPlayerNoDiscardCount, boolean thenEffectUsesDiscardedManaValue) {

    public DiscardFollowUp(int rummageDrawCount, UUID untapPermanentId,
                           List<UUID> remainingEachPlayerDiscards,
                           UUID eachPlayerControllerId, int eachPlayerAmount,
                           int graveyardReturnCount, List<Integer> eachPlayerAmounts,
                           UUID boostPermanentId, int boostPower, int boostToughness,
                           Card thenEffectSourceCard, CardEffect thenEffect) {
        this(rummageDrawCount, untapPermanentId, remainingEachPlayerDiscards, eachPlayerControllerId,
                eachPlayerAmount, graveyardReturnCount, eachPlayerAmounts, boostPermanentId, boostPower,
                boostToughness, thenEffectSourceCard, thenEffect, null, null, null, null, 0,
                null, false, 0, false);
    }

    public static final DiscardFollowUp NONE =
            new DiscardFollowUp(0, null, List.of(), null, 0, 0, List.of(), null, 0, 0,
                    null, null, null, null, null, null, 0, null, false, 0, false);

    public DiscardFollowUp {
        remainingEachPlayerDiscards = List.copyOf(remainingEachPlayerDiscards);
        eachPlayerAmounts = List.copyOf(eachPlayerAmounts);
    }

    public static DiscardFollowUp rummage(int drawCount) {
        return new DiscardFollowUp(drawCount, null, List.of(), null, 0, 0, List.of(), null, 0, 0,
                null, null, null, null, null, null, 0, null, false, 0, false);
    }

    public static DiscardFollowUp untap(UUID permanentId) {
        return new DiscardFollowUp(0, permanentId, List.of(), null, 0, 0, List.of(), null, 0, 0,
                null, null, null, null, null, null, 0, null, false, 0, false);
    }

    /** Source gets +power/+toughness until end of turn once the discard completes. */
    public static DiscardFollowUp boost(UUID permanentId, int power, int toughness) {
        return new DiscardFollowUp(0, null, List.of(), null, 0, 0, List.of(), permanentId, power, toughness,
                null, null, null, null, null, null, 0, null, false, 0, false);
    }

    /** Put a fixed number of +1/+1 counters on a permanent once the discard completes. */
    public static DiscardFollowUp plusOnePlusOneCounters(UUID permanentId, int amount) {
        return new DiscardFollowUp(0, null, List.of(), null, 0, 0, List.of(), null, 0, 0,
                null, null, null, null, null, permanentId, amount, null, false, 0, false);
    }

    public static DiscardFollowUp eachPlayer(List<UUID> remainingChoosers, UUID controllerId, int amount) {
        return new DiscardFollowUp(0, null, remainingChoosers, controllerId, amount, 0, List.of(), null, 0, 0,
                null, null, null, null, null, null, 0, null, false, 0, false);
    }

    /** Each-player discard with a reflexive effect after the whole queue finishes. */
    public static DiscardFollowUp eachPlayerWithThenEffect(List<UUID> remainingChoosers,
            UUID controllerId, int amount, Card sourceCard, CardEffect thenEffect) {
        return new DiscardFollowUp(0, null, remainingChoosers, controllerId, amount, 0, List.of(),
                null, 0, 0, sourceCard, thenEffect, null, null, null, null, 0,
                null, false, 0, false);
    }

    /** Carries the APNAP discard-choice remainder for Plaguecrafter. */
    public static DiscardFollowUp plaguecrafter(List<UUID> remainingChoosers) {
        return new DiscardFollowUp(0, null, remainingChoosers, null, 0, 0, List.of(), null, 0, 0,
                null, null, null, null, null, null, 0, null, true, 0, false);
    }

    /**
     * Each-player discard where each chooser discards their own amount ({@code amounts} parallel to
     * {@code remainingChoosers}). Used when the count is computed per player (Pox: a third of each
     * player's own hand, rounded up).
     */
    public static DiscardFollowUp eachPlayerVariableAmounts(List<UUID> remainingChoosers, UUID controllerId,
            List<Integer> amounts) {
        return new DiscardFollowUp(0, null, remainingChoosers, controllerId, 0, 0, amounts, null, 0, 0,
                null, null, null, null, null, null, 0, null, false, 0, false);
    }

    /** Return that many cards from the controller's graveyard to hand once the discard completes. */
    public static DiscardFollowUp graveyardReturn(int returnCount) {
        return new DiscardFollowUp(0, null, List.of(), null, 0, returnCount, List.of(), null, 0, 0,
                null, null, null, null, null, null, 0, null, false, 0, false);
    }

    /**
     * Push {@code thenEffect} onto the stack as a reflexive triggered ability once the discard
     * completes ("discard a [matching] card. If you do, [effect]").
     */
    public static DiscardFollowUp thenEffect(Card sourceCard, CardEffect thenEffect) {
        return thenEffect(sourceCard, thenEffect, null);
    }

    /** Push {@code thenEffect} only when the discarded card also matches {@code condition}. */
    public static DiscardFollowUp thenEffect(Card sourceCard, CardEffect thenEffect,
                                              CardPredicate condition) {
        return thenEffect(sourceCard, thenEffect, condition, null);
    }

    /**
     * Pushes {@code thenEffect} after the discard while preserving a pre-bound non-targeting
     * permanent reference through the interactive discard choice.
     */
    public static DiscardFollowUp thenEffect(Card sourceCard, CardEffect thenEffect,
                                              CardPredicate condition, UUID thenEffectTargetId) {
        return new DiscardFollowUp(0, null, List.of(), null, 0, 0, List.of(), null, 0, 0,
                sourceCard, thenEffect, condition, null, null, null, 0,
                thenEffectTargetId, false, 0, false);
    }

    /** Push a reflexive effect whose target filters use the discarded card's mana value as X. */
    public static DiscardFollowUp thenEffectWithDiscardedManaValue(Card sourceCard, CardEffect thenEffect) {
        return new DiscardFollowUp(0, null, List.of(), null, 0, 0, List.of(), null, 0, 0,
                sourceCard, thenEffect, null, null, null, null, 0, null, false, 0, true);
    }

    /** Completes a permanent's entry after the controller discards the required card. */
    public static DiscardFollowUp enteringPermanent(Permanent permanent, UUID controllerId) {
        return new DiscardFollowUp(0, null, List.of(), null, 0, 0, List.of(), null, 0, 0,
                null, null, null, permanent, controllerId, null, 0, null, false, 0, false);
    }

    /**
     * The same follow-up with the each-player remainder (both choosers and their per-player amounts)
     * advanced past the current chooser.
     */
    public DiscardFollowUp withRemainingEachPlayer(List<UUID> remaining, List<Integer> remainingAmounts) {
        return new DiscardFollowUp(rummageDrawCount, untapPermanentId, remaining,
                eachPlayerControllerId, eachPlayerAmount, graveyardReturnCount, remainingAmounts,
                boostPermanentId, boostPower, boostToughness, thenEffectSourceCard, thenEffect,
                thenEffectCondition,
                enteringPermanent, enteringControllerId, plusOnePlusOneCounterPermanentId,
                plusOnePlusOneCounterAmount, thenEffectTargetId, plaguecrafter,
                eachPlayerNoDiscardCount,
                thenEffectUsesDiscardedManaValue);
    }

    /** Records an opponent who had no card to discard. */
    public DiscardFollowUp incrementEachPlayerNoDiscardCount() {
        return new DiscardFollowUp(rummageDrawCount, untapPermanentId, remainingEachPlayerDiscards,
                eachPlayerControllerId, eachPlayerAmount, graveyardReturnCount, eachPlayerAmounts,
                boostPermanentId, boostPower, boostToughness, thenEffectSourceCard, thenEffect,
                thenEffectCondition, enteringPermanent, enteringControllerId,
                plusOnePlusOneCounterPermanentId, plusOnePlusOneCounterAmount,
                thenEffectTargetId, plaguecrafter,
                eachPlayerNoDiscardCount + 1, thenEffectUsesDiscardedManaValue);
    }
}
