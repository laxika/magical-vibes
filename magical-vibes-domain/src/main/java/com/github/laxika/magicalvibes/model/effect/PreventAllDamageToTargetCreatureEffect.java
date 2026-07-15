package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents all damage that would be dealt to the target creature this turn
 * (e.g. Wellgabber Apothecary). The target is added to
 * {@code GameData.creaturesWithAllDamagePrevented} and cleared at turn cleanup.
 */
public record PreventAllDamageToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
