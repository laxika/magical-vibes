package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exile target card from a graveyard. If it was a creature card, create a 2/2 black Zombie
 * creature token. Used by Deluge of the Dead.
 */
public record ExileGraveyardCardCreateTokenIfCreatureEffect() implements CardEffect, TokenCreatingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.ANY_GRAVEYARD_CARD);
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new Fixed(1);
    }

    @Override
    public CardType tokenType() {
        return CardType.CREATURE;
    }

    @Override
    public int tokenPower() {
        return 2;
    }

    @Override
    public int tokenToughness() {
        return 2;
    }
}
