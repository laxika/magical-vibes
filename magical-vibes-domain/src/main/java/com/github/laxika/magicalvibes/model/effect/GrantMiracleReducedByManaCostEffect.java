package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static effect that grants matching cards in hand miracle for their mana cost reduced by a fixed generic amount. */
public record GrantMiracleReducedByManaCostEffect(CardPredicate filter, int genericReduction)
        implements MiracleGrantingEffect {

    @Override
    public CardPredicate miracleGrantFilter() {
        return filter;
    }

    @Override
    public String miracleCostFor(Card card) {
        if (card == null || card.getParsedManaCost() == null
                || card.getManaCost() == null || card.getManaCost().isBlank()) {
            return null;
        }
        return card.getParsedManaCost()
                .reducedBy(new ManaCost("{" + genericReduction + "}"))
                .toManaCostString();
    }
}
