package com.github.laxika.magicalvibes.model.effect;

/**
 * Redirects a spell that has exactly one target to a new legal target chosen by this effect's controller.
 *
 * @param creatureTargetsOnly when {@code true} the redirection only happens if the target spell's single
 *                            target is a creature, and the new target must be another creature (Meddle).
 *                            When {@code false} any single target may be redirected (Deflection, Shunt, Swerve).
 */
public record ChangeTargetOfTargetSpellWithSingleTargetEffect(boolean creatureTargetsOnly) implements CardEffect {

    public ChangeTargetOfTargetSpellWithSingleTargetEffect() {
        this(false);
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.spellOnStack()); }
}
