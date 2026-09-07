package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect that grants matching nonland cards in a graveyard escape with an additional
 * graveyard exile requirement.
 */
public record GrantEscapeToGraveyardCardsEffect(CardPredicate filter)
        implements CastSpellsFromGraveyardPermission {

    @Override
    public int additionalGraveyardExileCount() {
        return 3;
    }

    @Override
    public boolean escape() {
        return true;
    }

    @Override
    public String additionalGraveyardExileLabel() {
        return "other cards";
    }
}
