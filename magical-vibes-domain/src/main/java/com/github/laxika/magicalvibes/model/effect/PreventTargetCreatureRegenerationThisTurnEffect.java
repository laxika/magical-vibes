package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target creature can't be regenerated this turn." Marks the target creature so that
 * {@code GraveyardService.tryRegenerate} refuses to regenerate it for the rest of the turn,
 * regardless of any regeneration shields it already has or later gains. The flag is cleared
 * during turn cleanup. Harmful (protection from the source is honoured). Hurr Jackal.
 * <p>
 * Also a {@link CombatOpponentReferencingEffect}: on {@code ON_BLOCK} /
 * {@code ON_BECOMES_BLOCKED} (PER_BLOCKER) the combat opponent is carried as the trigger's
 * non-targeting target (Lim-Dûl's Cohort).
 */
public record PreventTargetCreatureRegenerationThisTurnEffect()
        implements CardEffect, CombatOpponentReferencingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }
}
