package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Cost effect that requires exiling X cards from the controller's graveyard.
 * For spells, the number of exiled cards becomes the spell's X value (e.g. Harvest Pyre),
 * and the player chooses which cards to exile and how many. For activated abilities, the
 * announced mana X determines how many cards are exiled.
 *
 * @param requiredType the card type every exiled card must have (null = any, e.g. Haunting Misery's creature cards)
 * @param requireAtLeastOne whether the cost cannot be paid with X equal to zero
 */
public record ExileXCardsFromGraveyardCost(CardType requiredType, boolean requireAtLeastOne) implements CostEffect {

    public ExileXCardsFromGraveyardCost() {
        this(null, false);
    }

    public ExileXCardsFromGraveyardCost(CardType requiredType) {
        this(requiredType, false);
    }

    @Override
    public CardType consumedGraveyardCardType() {
        return requiredType;
    }
}
