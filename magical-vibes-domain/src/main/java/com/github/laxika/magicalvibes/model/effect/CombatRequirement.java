package com.github.laxika.magicalvibes.model.effect;

/**
 * Which one-shot combat requirement a {@link SetCombatRequirementThisTurnEffect} stamps onto its
 * target creature. Each constant maps to exactly one transient {@code Permanent} flag, all of which
 * are cleared together at end of turn by {@code Permanent.resetModifiers()}.
 *
 * <p>Requirements are obeyed only as far as the declaration rules allow, and per CR 508.1d /
 * CR 509.1c the affected player is never required to pay a cost to attack or block.
 */
public enum CombatRequirement {

    /** "Target creature attacks this turn if able" — any legal defender may be chosen (Incite). */
    MUST_ATTACK,

    /**
     * "Target creature attacks you this turn if able" (Alluring Siren) — the creature must attack the
     * effect's controller specifically, not that player's planeswalkers.
     */
    MUST_ATTACK_EFFECT_CONTROLLER,

    /**
     * "Target creature blocks this turn if able" (Nacatl Hunt-Pride) — the general requirement with no
     * particular attacker in mind, unlike Provoke's {@link MustBlockSourceEffect}.
     */
    MUST_BLOCK,

    /** "Target creature must be blocked this turn if able" (Emergent Growth). */
    MUST_BE_BLOCKED,

    /** "All creatures able to block target creature this turn do so" — one-shot Lure (Alluring Scent). */
    MUST_BE_BLOCKED_BY_ALL
}
