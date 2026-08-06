package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Return up to maxTargets target cards matching the filter from your graveyard to your hand.
 * Multi-target graveyard selection is handled by SpellCastingService at cast time.
 * Targets are stored in StackEntry.targetCardIds and resolved by GraveyardReturnResolutionService.
 *
 * @param filter            which graveyard cards may be chosen; {@code null} matches any card
 * @param maxTargets        the fixed cap on chosen cards; ignored when {@code dynamicMaxTargets}
 *                          is set
 * @param dynamicMaxTargets a cast-time cap computed from the game state instead of a fixed number
 *                          ("up to X target cards … where X is …", Reap). Evaluated as the spell is
 *                          cast, after its player target is chosen, so the effect also declares a
 *                          player {@link TargetSpec} in that case
 */
public record ReturnTargetCardsFromGraveyardToHandEffect(
        CardPredicate filter,
        int maxTargets,
        DynamicAmount dynamicMaxTargets
) implements CardEffect {

    public ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate filter, int maxTargets) {
        this(filter, maxTargets, null);
    }

    /** The dynamic-cap form: the cap is counted off the targeted player as the spell is cast. */
    public ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate filter, DynamicAmount dynamicMaxTargets) {
        this(filter, 0, dynamicMaxTargets);
    }

    @Override
    public TargetSpec targetSpec() {
        return dynamicMaxTargets == null
                ? TargetSpec.NONE
                : TargetSpec.benign(TargetPredicates.player());
    }
}
