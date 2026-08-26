package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * Equipment-scoped trigger marker for creating the supplied token when the equipped creature deals
 * combat damage to a creature.
 *
 * <p>The damage trigger collector expands this marker into the wrapped token creation effect, so
 * the marker itself is never resolved directly.</p>
 */
public record CreateTokenWhenEquippedCreatureDealsCombatDamageToCreatureEffect(CreateTokenEffect token)
        implements DamagedCreatureTriggerEffect, TokenCreatingEffect {

    public CreateTokenWhenEquippedCreatureDealsCombatDamageToCreatureEffect {
        Objects.requireNonNull(token, "token");
    }

    @Override
    public CardEffect triggeredEffect() {
        return token;
    }

    @Override
    public boolean equipmentScoped() {
        return true;
    }

    @Override
    public boolean combatDamageOnly() {
        return true;
    }

    @Override
    public DynamicAmount tokenAmount() {
        return token.amount();
    }

    @Override
    public CardType tokenType() {
        return token.primaryType();
    }

    @Override
    public int tokenPower() {
        return token.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return token.tokenToughness();
    }
}
