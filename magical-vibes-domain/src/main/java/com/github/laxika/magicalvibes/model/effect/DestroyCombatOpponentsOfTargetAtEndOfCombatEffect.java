package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose target creature. At this turn's next end of combat, destroy all creatures that blocked or
 * were blocked by it this turn." (Venomous Breath)
 * <p>
 * At resolution a {@link com.github.laxika.magicalvibes.model.action.DestroyCombatOpponentsAtEndOfCombat}
 * delayed action is queued for the target. The set of creatures to destroy is only computed when
 * that action is drained in {@code CombatService.processEndOfCombatCombatOpponentDestructions()},
 * so blocks declared after this spell resolved count too; regeneration and indestructible apply.
 * <p>
 * Unlike {@link DestroyCombatOpponentAtEndOfCombatEffect}, which is a Basilisk-style combat trigger
 * on the source permanent, this is a one-shot spell effect keyed to a chosen target.
 */
public record DestroyCombatOpponentsOfTargetAtEndOfCombatEffect() implements CardEffect, RemovalEffect {

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }
}
