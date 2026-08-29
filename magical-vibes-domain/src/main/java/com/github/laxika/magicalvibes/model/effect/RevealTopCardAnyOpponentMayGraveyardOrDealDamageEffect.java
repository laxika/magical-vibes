package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import java.util.UUID;

/**
 * Reveals the controller's top card. Each opponent may put it into the controller's graveyard;
 * the first opponent to do so is dealt damage equal to that card's mana value. If every opponent
 * declines, the card is put into the controller's hand.
 */
public record RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect(
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        UUID sourcePermanentId,
        UUID revealedCardId,
        int manaValue
) implements DamageDealingEffect {

    public RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect() {
        this(null, null, null, null, 0);
    }

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(manaValue);
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
