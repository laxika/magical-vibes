package com.github.laxika.magicalvibes.model.effect;

/**
 * The stack entry's source permanent ({@code sourcePermanentId}) assigns no combat damage this
 * turn — added to {@code GameData.creaturesPreventedFromDealingCombatDamage} and {@code GameData.creaturesAssigningNoCombatDamage}, cleared at turn
 * cleanup. Pair with damage (or similar) inside a {@link MayEffect}/{@link SequenceEffect} for
 * "you may [effect]. If you do, it assigns no combat damage this turn" (Gaze of Pain).
 *
 * <p>When {@code useTargetId} is true the creature is the stack entry's {@code targetId} instead, for
 * abilities whose source permanent is something else (Delif's Cube's delayed trigger: the Cube is the
 * source, "it" is the watched attacker). It applies even if the source has left the battlefield.</p>
 */
public record AssignNoCombatDamageEffect(boolean useTargetId) implements CardEffect {

    public AssignNoCombatDamageEffect() {
        this(false);
    }

    /** The entry's {@code targetId} creature assigns no combat damage this turn. */
    public static AssignNoCombatDamageEffect ofTargetId() {
        return new AssignNoCombatDamageEffect(true);
    }
}
