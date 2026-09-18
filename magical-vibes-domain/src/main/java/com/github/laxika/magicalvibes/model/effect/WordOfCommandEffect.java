package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

/** Looks at an opponent's hand and lets the controller choose a card to play. */
public record WordOfCommandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.players(
                new PlayerRelationPredicate(PlayerRelation.ANY)));
    }
}
