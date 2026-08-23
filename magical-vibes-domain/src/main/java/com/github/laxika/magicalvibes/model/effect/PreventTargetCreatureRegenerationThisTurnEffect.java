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
public record PreventTargetCreatureRegenerationThisTurnEffect(boolean sourcePermanent)
        implements CardEffect, CombatOpponentReferencingEffect {

    public PreventTargetCreatureRegenerationThisTurnEffect() {
        this(false);
    }

    /** Non-targeting source-scoped form for abilities such as Knight of the Holy Nimbus. */
    public static PreventTargetCreatureRegenerationThisTurnEffect forSourcePermanent() {
        return new PreventTargetCreatureRegenerationThisTurnEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        if (sourcePermanent) {
            return new TargetSpec(null, false, null, true, 1);
        }
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
