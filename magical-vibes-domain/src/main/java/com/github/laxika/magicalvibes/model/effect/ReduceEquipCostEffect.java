package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces the generic cost of equip abilities controlled by this permanent's controller.
 */
public record ReduceEquipCostEffect(int amount) implements ActivatedAbilityCostReducingEffect {

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
        return ability.getEffects().stream().anyMatch(EquipEffect.class::isInstance);
    }
}
