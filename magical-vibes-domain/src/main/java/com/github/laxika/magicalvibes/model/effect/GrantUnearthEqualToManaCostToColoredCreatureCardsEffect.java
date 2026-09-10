package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Static effect that gives matching colored creature cards in its controller's graveyard unearth
 * for a cost equal to each card's mana cost.
 */
public record GrantUnearthEqualToManaCostToColoredCreatureCardsEffect(Set<CardColor> colors)
        implements GraveyardAbilityGrantingEffect {

    @Override
    public boolean appliesTo(Card card) {
        return card != null && card.hasType(CardType.CREATURE)
                && card.getColors().stream().anyMatch(colors::contains);
    }

    @Override
    public ActivatedAbility grantedGraveyardAbilityFor(Card card) {
        if (card == null || card.getManaCost() == null || card.getManaCost().isBlank()) {
            return null;
        }
        return Card.unearthAbility(card.getManaCost());
    }
}
