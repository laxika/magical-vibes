package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents damage that would be dealt to the target creature this turn.
 * With {@code combatOnly=false} (Wellgabber Apothecary) all damage is prevented and the target is added to
 * {@code GameData.creaturesWithAllDamagePrevented}. With {@code combatOnly=true} (Foxfire) only combat damage
 * is prevented via {@code GameData.creaturesWithCombatDamagePrevented}. Both are cleared at turn cleanup.
 */
public record PreventAllDamageToTargetCreatureEffect(boolean combatOnly) implements CardEffect {

    public PreventAllDamageToTargetCreatureEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
