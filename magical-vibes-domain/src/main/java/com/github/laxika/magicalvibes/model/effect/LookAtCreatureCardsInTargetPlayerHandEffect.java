package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

/** Privately looks at the creature cards in a target opponent's hand. */
public record LookAtCreatureCardsInTargetPlayerHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.players(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT)));
    }
}
