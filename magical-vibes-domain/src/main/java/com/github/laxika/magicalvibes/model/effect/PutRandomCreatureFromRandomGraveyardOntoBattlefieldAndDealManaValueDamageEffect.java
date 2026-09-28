package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/**
 * Puts a random creature card from a random graveyard containing a creature card onto the
 * battlefield under the effect controller's control, then deals that card's mana value as damage
 * to the controller.
 */
public record PutRandomCreatureFromRandomGraveyardOntoBattlefieldAndDealManaValueDamageEffect()
        implements DamageDealingEffect {

    @Override
    public DynamicAmount damageAmount() {
        return new EventValue();
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }

    @Override
    public boolean damagesController() {
        return true;
    }
}
