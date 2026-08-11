package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Cost effect that requires exiling X cards from the controller's graveyard.
 * The number of exiled cards becomes the X value for the spell or activated ability.
 * The player chooses which cards to exile and how many (0 or more).
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
