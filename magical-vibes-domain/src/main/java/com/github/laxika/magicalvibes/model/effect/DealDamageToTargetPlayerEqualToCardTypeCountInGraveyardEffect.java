package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Deals damage to target player equal to the number of cards of the specified type
 * in the controller's graveyard.
 *
 * @param cardType the card type to count in the graveyard (e.g. ARTIFACT)
 */
public record DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffect(CardType cardType) implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
