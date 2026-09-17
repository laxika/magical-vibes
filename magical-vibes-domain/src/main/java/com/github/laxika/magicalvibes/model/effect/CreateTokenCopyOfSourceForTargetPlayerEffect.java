package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

/** Creates a plain copy of the source permanent under the targeted player's control. */
public record CreateTokenCopyOfSourceForTargetPlayerEffect(PlayerRelation targetPlayerRelation)
        implements CardEffect {

    public CreateTokenCopyOfSourceForTargetPlayerEffect() {
        this(PlayerRelation.ANY);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(targetPlayerRelation == PlayerRelation.ANY
                ? TargetPredicates.player()
                : TargetPredicates.players(new PlayerRelationPredicate(targetPlayerRelation)));
    }
}
