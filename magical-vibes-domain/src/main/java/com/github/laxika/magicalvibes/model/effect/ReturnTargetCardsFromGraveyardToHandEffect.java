package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Return up to maxTargets target cards matching the filter from your graveyard to your hand.
 * Multi-target graveyard selection is handled by SpellCastingService at cast time.
 * Targets are stored in StackEntry.targetCardIds and resolved by GraveyardReturnResolutionService.
 */
public record ReturnTargetCardsFromGraveyardToHandEffect(
        CardPredicate filter,
        int maxTargets
) implements CardEffect {
}
