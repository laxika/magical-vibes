package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

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
 */
public record ReturnTargetCardsFromGraveyardToHandEffect(
        CardPredicate filter,
        int maxTargets,
        DynamicAmount dynamicMaxTargets,
        boolean xScaled,
        boolean exactTargets,
        int minTargets,
        boolean requireSharedCreatureType
) implements CardEffect {

    public ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate filter, int maxTargets) {
        this(filter, maxTargets, null, false, false, 0, false);
    }

    /** The dynamic-cap form: the cap is counted off the targeted player as the spell is cast. */
    public ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate filter, DynamicAmount dynamicMaxTargets) {
        this(filter, 0, dynamicMaxTargets, false, false, 0, false);
    }

    /** Exact-X form: choose exactly the spell's paid X matching cards (Shattered Crypt). */
    public ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate filter, int maxTargets, boolean xScaled) {
        this(filter, maxTargets, null, xScaled, false, 0, false);
    }

    /** Fixed-exact form: choose exactly {@code targetCount} matching cards (Death's Duet). */
    public static ReturnTargetCardsFromGraveyardToHandEffect exactly(CardPredicate filter, int targetCount) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, targetCount, null, false, true, targetCount, false);
    }

    public static ReturnTargetCardsFromGraveyardToHandEffect exactlyOne(CardPredicate filter) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(filter, 1, null, false, false, 1, false);
    }

    public static ReturnTargetCardsFromGraveyardToHandEffect exactlyTwoSharingCreatureType(CardPredicate filter) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(filter, 2, null, false, false, 2, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return dynamicMaxTargets == null
                ? TargetSpec.NONE
                : TargetSpec.benign(TargetPredicates.player());
    }
}
