package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.UUID;

/**
 * Reduces the generic cost of an activated ability of an Equipment controlled by this permanent's
 * controller when that ability targets this permanent.
 */
public record ReduceActivatedAbilityCostForTargetingSourceEffect(int amount)
        implements ActivatedAbilityCostReducingEffect {

    private static final PermanentPredicate EQUIPMENT = new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT);

    @Override
    public PermanentPredicate affectedPermanents() {
        return EQUIPMENT;
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
        return reducingPermanentId != null
                && (reducingPermanentId.equals(targetId)
                || targetIds != null && targetIds.contains(reducingPermanentId));
    }

    @Override
    public boolean appliesSymmetrically() {
        return false;
    }
}
