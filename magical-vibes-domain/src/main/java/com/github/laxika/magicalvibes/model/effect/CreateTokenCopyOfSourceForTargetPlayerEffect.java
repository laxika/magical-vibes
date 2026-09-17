package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

/** Creates a token copy of the source permanent under the targeted player's control. */
public record CreateTokenCopyOfSourceForTargetPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.players(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT)));
    }
}
