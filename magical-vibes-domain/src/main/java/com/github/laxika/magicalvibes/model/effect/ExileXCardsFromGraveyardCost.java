package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Cost effect that requires exiling X cards from the controller's graveyard.
 * For spells, the number of exiled cards becomes the spell's X value (e.g. Harvest Pyre),
 * and the player chooses which cards to exile and how many. For activated abilities, the
 * announced mana X determines how many cards are exiled.
 *
 * @param requiredType the card type every exiled card must have (null = any, e.g. Haunting Misery's creature cards)
 */
public record ExileXCardsFromGraveyardCost(CardType requiredType) implements CostEffect {

    public ExileXCardsFromGraveyardCost() {
        this(null);
    }

    @Override
    public CardType consumedGraveyardCardType() {
        return requiredType;
    }
}
