package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

import static com.github.laxika.magicalvibes.model.effect.TargetPredicates.anyOf;
import static com.github.laxika.magicalvibes.model.effect.TargetPredicates.permanents;
import static com.github.laxika.magicalvibes.model.effect.TargetPredicates.players;

/** Removes up to a capped number of counters of the controller's choice from a target. */
public record RemoveUpToCountersFromTargetEffect(int maxAmount, PermanentPredicate permanentPredicate)
        implements CardEffect {

    public RemoveUpToCountersFromTargetEffect(int maxAmount) {
        this(maxAmount, new PermanentTruePredicate());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(anyOf(
                players(new PlayerRelationPredicate(PlayerRelation.OPPONENT)),
                permanents(permanentPredicate)));
    }
}
