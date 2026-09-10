package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Offers to put one face-up card owned by an opponent from exile into that player's graveyard,
 * then gives the effect's controller life if a card was put there.
 */
public record PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffect(int lifeGain)
        implements LifeGainEffect {

    @Override
    public DynamicAmount lifeGainAmount() {
        return new Fixed(lifeGain);
    }
}
