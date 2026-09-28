package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.UUID;

/** Reduces the first qualifying activated ability's generic cost by a fixed amount each turn. */
public record ReduceFirstActivatedAbilityCostForControlledCreatureEffect(int amount)
        implements FirstActivatedAbilityCostReducingEffect {

    private static final PermanentPredicate ALL_PERMANENTS = new PermanentTruePredicate();
    private static final String USAGE_KEY = "first-activated-ability-targeting-controlled-creature";

    @Override
    public String usageKey() {
        return USAGE_KEY;
    }

    @Override
    public PermanentPredicate affectedPermanents() {
        return ALL_PERMANENTS;
    }

    @Override
    public int genericCostReduction() {
        return amount;
    }

    @Override
    public boolean appliesTo(ActivatedAbility ability) {
        return false;
    }

    @Override
    public boolean appliesTo(ActivatedAbility ability, UUID reducingPermanentId,
                             UUID targetId, List<UUID> targetIds) {
        return targetId != null || targetIds != null && !targetIds.isEmpty();
    }

    @Override
    public boolean appliesSymmetrically() {
        return false;
    }

    @Override
    public boolean requiresControlledCreatureTarget() {
        return true;
    }
}
