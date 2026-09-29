package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/**
 * Puts a random creature card with mana value at most the evaluated amount from the targeted
 * opponent's library onto the effect controller's battlefield, giving it perpetual X/X and ward.
 */
public record PutRandomCreatureFromTargetOpponentLibraryOntoBattlefieldEffect(
        DynamicAmount powerAndToughness) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return PlayerRelation.OPPONENT;
    }
}
