package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect that lets the controller cast matching spells from their graveyard for their
 * normal mana costs.
 */
public record CastSpellsFromGraveyardEffect(CardPredicate filter)
        implements CastSpellsFromGraveyardPermission {
}
