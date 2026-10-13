package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Reduces the generic cost of equip abilities controlled by this permanent's controller.
 *
 * @param otherEquipmentOnly when true, the source Equipment's own equip ability is excluded
 * @param sourceOnly when true, only the source Equipment's own equip ability is reduced ("This
 *                   Equipment's equip abilities cost {N} less"); takes precedence over
 *                   {@code otherEquipmentOnly}
 */
public record ReduceEquipCostEffect(int amount, boolean otherEquipmentOnly, boolean sourceOnly)
        implements ActivatedAbilityCostReducingEffect {

    private static final PermanentPredicate EQUIPMENT = new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT);
    private static final PermanentPredicate OTHER_EQUIPMENT = new PermanentAllOfPredicate(List.of(
            EQUIPMENT,
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
    private static final PermanentPredicate SOURCE_EQUIPMENT = new PermanentAllOfPredicate(List.of(
            EQUIPMENT,
            new PermanentIsSourcePermanentPredicate()));

    public ReduceEquipCostEffect(int amount) {
        this(amount, false, false);
    }

    public ReduceEquipCostEffect(int amount, boolean otherEquipmentOnly) {
        this(amount, otherEquipmentOnly, false);
    }

    /** Reduces only the equip abilities of the Equipment this effect is printed on or granted to. */
    public static ReduceEquipCostEffect ofThisEquipmentOnly(int amount) {
        return new ReduceEquipCostEffect(amount, false, true);
    }

    @Override
    public PermanentPredicate affectedPermanents() {
        if (sourceOnly) {
            return SOURCE_EQUIPMENT;
        }
        return otherEquipmentOnly ? OTHER_EQUIPMENT : EQUIPMENT;
    }

    @Override
    public int genericCostReduction() {
        return amount;
    }

    @Override
    public boolean appliesTo(ActivatedAbility ability) {
        return ability.getEffects().stream().anyMatch(EquipEffect.class::isInstance);
    }

    @Override
    public boolean appliesSymmetrically() {
        return false;
    }
}
