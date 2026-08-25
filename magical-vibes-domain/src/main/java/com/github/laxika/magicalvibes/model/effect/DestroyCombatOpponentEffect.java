package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger that immediately destroys the creature this permanent blocks or that blocks it.
 * The combat opponent is carried as the trigger's non-targeting target.
 *
 * @param cannotBeRegenerated whether the destruction prevents regeneration
 */
public record DestroyCombatOpponentEffect(boolean cannotBeRegenerated)
        implements RemovalEffect, CombatOpponentReferencingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
