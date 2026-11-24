package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.Objects;

/**
 * "Reselect which player or permanent [attacker] is attacking": prompts the effect's controller for a new attack
 * target for the attacking creature {@code attacker} names ({@link Subjects#target()}: a benign target attacking
 * creature, Portal Mage and Misleading Signpost; {@link Subjects#source()}: this creature, Capricopian). Reselecting
 * ignores every requirement, restriction and cost of attacking (Portal Mage's ruling), so it offers what
 * {@code AttackLegalityService.getValidReselectionTargetIds} allows; it can't attack its controller or their
 * permanents. {@code playersOnly}: "which player".
 */
public record ReselectAttackingCreatureAttackTargetEffect(PermanentSubject attacker, boolean playersOnly)
        implements CardEffect {

    public ReselectAttackingCreatureAttackTargetEffect {
        Objects.requireNonNull(attacker, "attacker");
    }

    /** "Reselect which player or permanent target attacking creature is attacking." */
    public ReselectAttackingCreatureAttackTargetEffect() {
        this(Subjects.target(), false);
    }

    @Override
    public TargetSpec targetSpec() {
        if (attacker.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsAttackingPredicate());
        }
        return TargetSpec.NONE;
    }
}
