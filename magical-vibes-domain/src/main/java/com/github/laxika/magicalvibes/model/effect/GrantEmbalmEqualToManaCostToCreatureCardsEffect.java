package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

/**
 * Static effect that gives every creature card in the controller's graveyard embalm for its own
 * mana cost.
 */
public record GrantEmbalmEqualToManaCostToCreatureCardsEffect()
        implements GraveyardAbilityGrantingEffect {

    @Override
    public boolean appliesTo(Card card) {
        return card != null && card.hasType(CardType.CREATURE);
    }

    @Override
    public ActivatedAbility grantedGraveyardAbilityFor(Card card) {
        if (card == null || card.getManaCost() == null || card.getManaCost().isBlank()) {
            return null;
        }
        return Card.embalmAbility(card.getManaCost());
    }
}
