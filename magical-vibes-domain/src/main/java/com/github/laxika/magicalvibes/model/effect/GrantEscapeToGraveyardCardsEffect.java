package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect that grants matching cards in a graveyard escape with an additional graveyard
 * exile requirement.
 */
public record GrantEscapeToGraveyardCardsEffect(
        CardPredicate filter,
        String alternateManaCost,
        int additionalGraveyardExileCount
)
        implements CastSpellsFromGraveyardPermission {

    public GrantEscapeToGraveyardCardsEffect(CardPredicate filter) {
        this(filter, null, 3);
    }

    public GrantEscapeToGraveyardCardsEffect {
        if (additionalGraveyardExileCount < 0) {
            throw new IllegalArgumentException("additionalGraveyardExileCount must not be negative");
        }
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
