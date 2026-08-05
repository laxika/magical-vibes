package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: the referenced combat opponent is tapped at end of combat and doesn't untap during
 * its controller's next untap step. Joven's Ferrets' "At end of combat, tap all creatures that
 * blocked this creature this turn. They don't untap during their controller's next untap step."
 * <p>
 * Placed on {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_BECOMES_BLOCKED} with
 * {@link com.github.laxika.magicalvibes.model.TriggerMode#PER_BLOCKER} — one trigger per blocker, so
 * every creature that blocked the source this turn gets scheduled. The referenced creature rides as
 * the stack entry's non-targeting target, so the trigger can't fizzle. Resolution queues a
 * {@link com.github.laxika.magicalvibes.model.action.TapAndSkipUntapAtEndOfCombat} drained by
 * {@code CombatService.processEndOfCombatTaps()}, so the blocker still deals its combat damage.
 */
public record TapCombatOpponentAtEndOfCombatEffect() implements CardEffect, CombatOpponentReferencingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
