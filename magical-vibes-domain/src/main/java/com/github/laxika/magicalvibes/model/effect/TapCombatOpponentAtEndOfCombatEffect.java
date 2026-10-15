package com.github.laxika.magicalvibes.model.effect;

/**
 * Joven's Ferrets' "At end of combat, tap all creatures that blocked this creature this turn. They
 * don't untap during their controller's next untap step." Put on
 * {@link com.github.laxika.magicalvibes.model.EffectSlot#END_OF_COMBAT_TRIGGERED}: the trigger goes on
 * the stack as the end of combat step begins and reads the source's blockers as it resolves.
 */
public record TapCombatOpponentAtEndOfCombatEffect() implements CardEffect, CombatOpponentReferencingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
