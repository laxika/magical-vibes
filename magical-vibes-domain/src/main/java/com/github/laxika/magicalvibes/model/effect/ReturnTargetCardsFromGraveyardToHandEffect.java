package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Set;

/**
 * Return target cards matching the filter from your graveyard to your hand.
 * Multi-target graveyard selection is handled by SpellCastingService at cast time.
 * Targets are stored in StackEntry.targetCardIds and resolved by GraveyardReturnResolutionService.
 *
 * <p>By default, the effect allows up to {@code maxTargets} targets. When {@code exactTargets} is
 * set, exactly {@code maxTargets} targets must be chosen ("Return two target creature cards ..."
 * — Death's Duet).</p>
 *
 * <p>When {@code xScaled} is set the target count is the spell's paid X instead of
 * {@code maxTargets}, and exactly that many targets must be chosen ("Return X target creature
 * cards from your graveyard to your hand" — Shattered Crypt).</p>
 *
 * @param filter            which graveyard cards may be chosen; {@code null} matches any card
 * @param maxTargets        the fixed cap or exact count of chosen cards; ignored when
 *                          {@code dynamicMaxTargets} or {@code xScaled} is set
 * @param dynamicMaxTargets a cast-time cap computed from the game state instead of a fixed number
 *                          ("up to X target cards … where X is …", Reap). Evaluated as the spell is
 *                          cast, after its player target is chosen, so the effect also declares a
 *                          player {@link TargetSpec} in that case
 * @param xScaled           when {@code true} the target count is exactly the spell's paid X
 * @param exactTargets      when {@code true} exactly {@code maxTargets} targets must be chosen
 * @param maxOnePerCardType card types for which at most one selected card is allowed
 * @param unlessAnyPlayerPaysX when {@code true}, any player may pay the spell's X at resolution
 *                             to prevent the return effect
 * @param opponentChoosesOneForHand when {@code true}, an opponent chooses one of the selected
 *                                  cards to return to hand and the other returns to the battlefield
 *                                  with haste and a delayed exile
 */
public record ReturnTargetCardsFromGraveyardToHandEffect(
        CardPredicate filter,
        int maxTargets,
        DynamicAmount dynamicMaxTargets,
        boolean xScaled,
        boolean exactTargets,
        int minTargets,
        boolean requireSharedCreatureType,
        Set<CardType> maxOnePerCardType,
        boolean unlessAnyPlayerPaysX,
        boolean opponentChoosesOneForHand
) implements CardEffect {

    public ReturnTargetCardsFromGraveyardToHandEffect {
        maxOnePerCardType = maxOnePerCardType == null ? Set.of() : Set.copyOf(maxOnePerCardType);
    }

    public ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate filter, int maxTargets) {
        this(filter, maxTargets, null, false, false, 0, false, Set.of(), false, false);
    }

    public ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate filter, int maxTargets,
                                                       DynamicAmount dynamicMaxTargets,
                                                       boolean xScaled, boolean exactTargets,
                                                       int minTargets, boolean requireSharedCreatureType,
                                                       Set<CardType> maxOnePerCardType,
                                                       boolean unlessAnyPlayerPaysX) {
        this(filter, maxTargets, dynamicMaxTargets, xScaled, exactTargets, minTargets,
                requireSharedCreatureType, maxOnePerCardType, unlessAnyPlayerPaysX, false);
    }

    /** The dynamic-cap form: the cap is counted off the targeted player as the spell is cast. */
    public ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate filter, DynamicAmount dynamicMaxTargets) {
        this(filter, 0, dynamicMaxTargets, false, false, 0, false, Set.of(), false, false);
    }

    /** Exact-X form: choose exactly the spell's paid X matching cards (Shattered Crypt). */
    public ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate filter, int maxTargets, boolean xScaled) {
        this(filter, maxTargets, null, xScaled, false, 0, false, Set.of(), false, false);
    }

    /** Fixed-exact form: choose exactly {@code targetCount} matching cards (Death's Duet). */
    public static ReturnTargetCardsFromGraveyardToHandEffect exactly(CardPredicate filter, int targetCount) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, targetCount, null, false, true, targetCount, false, Set.of(), false, false);
    }

    /** Fixed-exact form with an X payment that prevents the return at resolution. */
    public static ReturnTargetCardsFromGraveyardToHandEffect exactlyUnlessAnyPlayerPaysX(
            CardPredicate filter, int targetCount) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, targetCount, null, false, true, targetCount, false, Set.of(), true, false);
    }

    public static ReturnTargetCardsFromGraveyardToHandEffect exactlyOne(CardPredicate filter) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(filter, 1, null, false, false, 1, false, Set.of(), false, false);
    }

    public static ReturnTargetCardsFromGraveyardToHandEffect exactlyTwoSharingCreatureType(CardPredicate filter) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(filter, 2, null, false, false, 2, true, Set.of(), false, false);
    }

    /** Return up to one card matching each listed card type. */
    public static ReturnTargetCardsFromGraveyardToHandEffect upToOnePerCardType(
            CardPredicate filter, Set<CardType> cardTypes) {
        if (cardTypes == null || cardTypes.isEmpty()) {
            throw new IllegalArgumentException("cardTypes must not be empty");
        }
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, cardTypes.size(), null, false, false, 0, false, cardTypes, false, false);
    }

    /** Return one selected card to hand and the other to the battlefield after an opponent chooses. */
    public static ReturnTargetCardsFromGraveyardToHandEffect opponentChoosesOneForHand(CardPredicate filter) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, 2, null, false, false, 0, false, Set.of(), false, true);
    }

    /** Returns an equivalent effect without the resolution-time payment clause. */
    public ReturnTargetCardsFromGraveyardToHandEffect withoutAnyPlayerPaysX() {
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, maxTargets, dynamicMaxTargets, xScaled, exactTargets, minTargets,
                requireSharedCreatureType, maxOnePerCardType, false, opponentChoosesOneForHand);
    }

    @Override
    public TargetSpec targetSpec() {
        return dynamicMaxTargets == null
                ? TargetSpec.NONE
                : TargetSpec.benign(TargetPredicates.player());
    }
}
