package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardHasCyclingPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static: "You may cast spells that have a cycling ability from your graveyard."
 * Used by Abandoned Sarcophagus.
 */
public record CastSpellsWithCyclingFromGraveyardEffect() implements CastSpellsFromGraveyardPermission {

    @Override
    public CardPredicate filter() {
        return new CardHasCyclingPredicate();
    }
}
