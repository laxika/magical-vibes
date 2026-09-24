package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

public record EquipEffect(TargetPredicate targetPredicate, boolean permitsNonCreatureTarget,
                          boolean permitsCreatureEquipment) implements CardEffect {

    public EquipEffect(TargetPredicate targetPredicate, boolean permitsNonCreatureTarget) {
        this(targetPredicate, permitsNonCreatureTarget, false);
    }

    public EquipEffect() {
        this(TargetPredicates.creature(), false, false);
    }

    public static EquipEffect toPlaneswalker() {
        return new EquipEffect(
                TargetPredicates.permanents(new PermanentIsPlaneswalkerPredicate()), true, false);
    }

    public static EquipEffect reconfigure(TargetPredicate targetPredicate) {
        return new EquipEffect(targetPredicate, false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(targetPredicate);
    }
}
