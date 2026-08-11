package com.github.laxika.magicalvibes.model.effect;

/**
 * Changes the target of a single-target spell unless that spell's controller pays a fixed amount
 * of generic mana.
 */
public record ChangeTargetOfTargetSpellUnlessControllerPaysEffect(int payAmount) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
