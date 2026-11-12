package com.github.laxika.magicalvibes.model.effect;

/**
 * The stack entry's source permanent ({@code sourcePermanentId}) assigns no combat damage this
 * turn — added to {@code GameData.creaturesPreventedFromDealingCombatDamage} and {@code GameData.creaturesAssigningNoCombatDamageThisTurn}, cleared at turn
 * cleanup. Pair with damage (or similar) inside a {@link MayEffect}/{@link SequenceEffect} for
 * "you may [effect]. If you do, it assigns no combat damage this turn" (Gaze of Pain).
 *
 * <p>When {@code useTargetId} is true the creature is the stack entry's {@code targetId} instead, for
 * abilities whose source permanent is something else (Delif's Cube's delayed trigger: the Cube is the
 * source, "it" is the watched attacker). It applies even if the source has left the battlefield.</p>
 *
 * <p>When {@code thisCombatOnly} is true the restriction ends with the current combat instead of
 * lasting the whole turn ("assigns no combat damage this combat").</p>
 */
public record AssignNoCombatDamageEffect(boolean useTargetId, boolean thisCombatOnly,
                                         boolean useTriggeringPermanent) implements CardEffect {

    public AssignNoCombatDamageEffect(boolean useTargetId, boolean thisCombatOnly) {
        this(useTargetId, thisCombatOnly, false);
    }

    public AssignNoCombatDamageEffect(boolean useTargetId) {
        this(useTargetId, false);
    }

    /**
     * The permanent the trigger event was about assigns no combat damage this turn ("the attacking creature assigns no
     * combat damage", Farrel's Mantle's enchanted creature).
     */
    public static AssignNoCombatDamageEffect ofTriggeringPermanent() {
        return new AssignNoCombatDamageEffect(false, false, true);
    }

    public AssignNoCombatDamageEffect() {
        this(false);
    }

    /** The source permanent assigns no combat damage this combat only (Master of Cruelties). */
    public static AssignNoCombatDamageEffect thisCombat() {
        return new AssignNoCombatDamageEffect(false, true);
    }

    /** The entry's {@code targetId} creature assigns no combat damage this turn. */
    public static AssignNoCombatDamageEffect ofTargetId() {
        return new AssignNoCombatDamageEffect(true);
    }
}
