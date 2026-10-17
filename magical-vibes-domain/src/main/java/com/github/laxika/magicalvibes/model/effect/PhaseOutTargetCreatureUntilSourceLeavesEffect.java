package com.github.laxika.magicalvibes.model.effect;

/**
 * Phases the target creature out and keeps it phased out until the source permanent leaves.
 * When {@code tapsOnPhaseIn} is {@code true} the creature is tapped as it phases back in (Oubliette);
 * otherwise it phases in with its tapped status unchanged (The Moment).
 *
 * @param tapsOnPhaseIn whether the creature is tapped as it phases in when the source leaves
 */
public record PhaseOutTargetCreatureUntilSourceLeavesEffect(boolean tapsOnPhaseIn) implements CardEffect {

    public PhaseOutTargetCreatureUntilSourceLeavesEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
