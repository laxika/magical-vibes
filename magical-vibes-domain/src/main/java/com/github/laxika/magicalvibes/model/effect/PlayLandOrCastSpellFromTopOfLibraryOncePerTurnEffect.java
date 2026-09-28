package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static permission for one matching land play or spell cast from the controller's library top
 * each turn.
 */
public record PlayLandOrCastSpellFromTopOfLibraryOncePerTurnEffect(CardPredicate filter)
        implements CardEffect {
}
