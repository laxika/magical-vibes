package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: the combat opponent's controller sacrifices it at end of combat, and — if they do —
 * creates {@code tokenForSacrificingPlayer}. Basalt Golem's "Whenever this creature becomes blocked
 * by a creature, that creature's controller sacrifices it at end of combat. If the player does, they
 * create a 0/2 colorless Wall artifact creature token with defender."
 * <p>
 * Placed on {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_BECOMES_BLOCKED} with
 * {@link com.github.laxika.magicalvibes.model.TriggerMode#PER_BLOCKER} (one trigger per blocker) and/or
 * on {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_BLOCK} for the "blocks" half. The
 * referenced creature is carried as the stack entry's non-targeting target, so the trigger can't
 * fizzle. Resolution queues a {@link com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat}
 * drained by {@code CombatService.processEndOfCombatSacrifices()} — the creature still deals its
 * combat damage, and sacrifice bypasses regeneration and indestructible.
 *
 * @param tokenForSacrificingPlayer token created for the sacrificing player, or {@code null} for no rider
 */
public record SacrificeCombatOpponentAtEndOfCombatEffect(
        CreateTokenEffect tokenForSacrificingPlayer
) implements CardEffect, CombatOpponentReferencingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
