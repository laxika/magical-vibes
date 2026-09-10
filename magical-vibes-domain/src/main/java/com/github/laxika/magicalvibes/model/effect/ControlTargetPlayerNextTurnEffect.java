package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

/**
 * You control target player during that player's next turn (Mindslaver).
 * When {@code grantExtraTurnAfter} is true (Emrakul, the Promised End), that player also takes
 * an extra turn after the controlled turn — scheduled when control actually activates so a skip
 * or loss during the controlled turn does not orphan an early-queued extra turn.
 */
public record ControlTargetPlayerNextTurnEffect(boolean grantExtraTurnAfter,
                                                PlayerRelation targetPlayerRelation) implements CardEffect {

    public ControlTargetPlayerNextTurnEffect() {
        this(false, PlayerRelation.ANY);
    }

    public ControlTargetPlayerNextTurnEffect(boolean grantExtraTurnAfter) {
        this(grantExtraTurnAfter, PlayerRelation.ANY);
    }

    public ControlTargetPlayerNextTurnEffect(PlayerRelation targetPlayerRelation) {
        this(false, targetPlayerRelation);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(targetPlayerRelation == PlayerRelation.ANY
                ? TargetPredicates.player()
                : TargetPredicates.players(new PlayerRelationPredicate(targetPlayerRelation)));
    }
}
