package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

/**
 * For each tapped nonland permanent the target opponent controls, searches that opponent's
 * library for a card with the same name and puts the found cards onto the battlefield under the
 * effect controller's control, then shuffles that library.
 */
public record DichotomancyEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.players(new PlayerRelationPredicate(PlayerRelation.OPPONENT)));
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return PlayerRelation.OPPONENT;
    }
}
