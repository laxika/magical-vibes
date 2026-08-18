package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Static effect for "each creature card in your graveyard has the chosen creature type in addition
 * to its other types." The chosen subtype is read from the source permanent while the card remains
 * in its controller's graveyard.
 */
public record GrantChosenSubtypeToOwnGraveyardCreatureCardsEffect()
        implements GraveyardSubtypeGrantingEffect {

    @Override
    public CardSubtype grantedGraveyardSubtypeFor(Permanent source, Card card) {
        return source.getChosenSubtype();
    }

    @Override
    public boolean appliesTo(Card card) {
        return card != null && card.hasType(CardType.CREATURE);
    }
}
