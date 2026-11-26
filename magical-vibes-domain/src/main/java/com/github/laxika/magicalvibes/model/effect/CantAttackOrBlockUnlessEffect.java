package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Static combat restriction on the creature carrying it: it can't attack, can't block, or can't attack or block
 * unless the given {@link Condition} is met (CR 508.1c, 509.1b). The attack side is judged as attackers are declared
 * ({@code AttackLegalityService}) and the block side as blockers are declared ({@code BlockLegalityService}), both
 * through {@code ConditionEvaluationService} with the creature as the source; a face-down creature or one that lost
 * all its abilities isn't restricted.
 * <p>
 * Factories, reading like the Oracle wording:
 * <ul>
 *   <li>{@link #attack} — "can't attack unless": Desperate Castaways ({@code ControlsPermanentCount(1, artifact)}),
 *       Sea Monster ({@code DefendingPlayerControlsPermanent(Island)}), Chained Throatseeker
 *       ({@code DefendingPlayerPoisoned()});</li>
 *   <li>{@link #block} — "can't block unless": Marauding Boneslasher ({@code ControlsOtherPermanentCount(1, Zombie)});</li>
 *   <li>{@link #attackOrBlock} — "can't attack or block unless": Blind-Spot Giant
 *       ({@code ControlsAnotherPermanent(Giant)}).</li>
 * </ul>
 *
 * @param condition              the condition that must be met for the creature to attack or block
 * @param requirementDescription human-readable "unless" clause (e.g. "you control another Giant")
 * @param restrictsAttacking     whether the creature can't attack unless the condition is met
 * @param restrictsBlocking      whether the creature can't block unless the condition is met
 */
public record CantAttackOrBlockUnlessEffect(Condition condition, String requirementDescription,
                                            boolean restrictsAttacking, boolean restrictsBlocking)
        implements AttackOrBlockRestrictionEffect, BlockingRestrictionEffect {

    public CantAttackOrBlockUnlessEffect {
        if (condition == null) {
            throw new IllegalArgumentException("A can't-attack-or-block-unless restriction needs a condition");
        }
        if (!restrictsAttacking && !restrictsBlocking) {
            throw new IllegalArgumentException("A can't-attack-or-block-unless restriction restricts attacking, blocking or both");
        }
    }

    /** "This creature can't attack unless [condition]." */
    public static CantAttackOrBlockUnlessEffect attack(Condition condition, String requirementDescription) {
        return new CantAttackOrBlockUnlessEffect(condition, requirementDescription, true, false);
    }

    /** "This creature can't block unless [condition]." */
    public static CantAttackOrBlockUnlessEffect block(Condition condition, String requirementDescription) {
        return new CantAttackOrBlockUnlessEffect(condition, requirementDescription, false, true);
    }

    /** "This creature can't attack or block unless [condition]." */
    public static CantAttackOrBlockUnlessEffect attackOrBlock(Condition condition, String requirementDescription) {
        return new CantAttackOrBlockUnlessEffect(condition, requirementDescription, true, true);
    }

    @Override
    public Condition cantAttackUnless() {
        return restrictsAttacking ? condition : null;
    }

    @Override
    public Condition cantBlockUnless() {
        return restrictsBlocking ? condition : null;
    }

    @Override
    public String restrictionDescription() {
        return requirementDescription;
    }
}
