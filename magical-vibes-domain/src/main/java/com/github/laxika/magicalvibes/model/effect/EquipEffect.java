package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

public record EquipEffect(TargetPredicate targetPredicate, boolean permitsNonCreatureTarget) implements CardEffect {

    public EquipEffect() {
        this(TargetPredicates.creature(), false);
    }

    public static EquipEffect toPlaneswalker() {
        return new EquipEffect(
                TargetPredicates.permanents(new PermanentIsPlaneswalkerPredicate()), true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(targetPredicate);
    }
}
