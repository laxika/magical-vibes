package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: schedule the source permanent (the creature this effect is on) for destruction
 * at end of combat. "When this creature blocks/attacks, destroy it at end of combat." (e.g. Cinder
 * Wall's "When this creature blocks, destroy it at end of combat"). Unlike
 * {@link SacrificeAtEndOfCombatEffect} this is a destruction, so regeneration and indestructible
 * apply. At resolution a delayed
 * {@link com.github.laxika.magicalvibes.model.action.DelayedPermanentAction} is queued for
 * the source permanent; it is drained in {@code CombatService.processEndOfCombatDestructions()}.
 */
public record DestroySelfAtEndOfCombatEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetCategory.NONE, true, null, false, 1);
    }
}
