package com.github.laxika.magicalvibes.service.combat.block;

/**
 * A failed block-legality check: the rule that denied the block, plus the detail needed to render
 * the user-facing message (a restriction's own description, or the land type name for landwalk).
 * {@code null} — rather than an instance of this record — is what a legal block looks like.
 *
 * <p>Produced by {@link BlockLegalityService}, rendered by {@link BlockDenialMessageService}.
 * Reasons that carry no detail reuse the shared constants declared below, so the boolean fast path
 * ({@link BlockLegalityService#canBlockAttacker}) allocates nothing per blocker × attacker pair.
 */
public record BlockDenial(Reason reason, String detail) {

    /**
     * Why one specific blocker may not block one specific attacker. Every constant is a distinct
     * rule; several can apply at once, in which case the pairwise check reports whichever it
     * evaluates first.
     */
    public enum Reason {

        /**
         * The attacker simply can't be blocked — either unconditionally, or because a conditional
         * "can't be blocked" clause is currently satisfied (defending player controls a given
         * permanent, controller cast a historic spell this turn, attacking alone). No blocker is legal.
         */
        CANT_BE_BLOCKED,

        /** CR 702.9b — the attacker has flying and the blocker has neither flying nor reach. */
        FLYING,

        /** CR 702.32b — the attacker has horsemanship and the blocker does not. */
        HORSEMANSHIP,

        /** CR 702.36b — the attacker has fear and the blocker is neither an artifact creature nor black. */
        FEAR,

        /**
         * CR 702.13b — the attacker has intimidate and the blocker is neither an artifact creature nor
         * shares a color with it.
         */
        INTIMIDATE,

        /** CR 702.129a — the attacker has skulk and the blocker's power is greater than its own. */
        SKULK,

        /** The attacker and blocker do not agree on shadow. */
        SHADOW,

        /**
         * The blocker carries a "can block only creatures with …" restriction that this attacker does
         * not match. {@link BlockDenial#detail()} is the phrase describing which attackers it may block.
         */
        BLOCKER_LIMITED_TO_ATTACKERS,

        /**
         * A board-wide "creatures matching X can't block creatures matching Y" static from some third
         * permanent applies to this pair (e.g. Boldwyr Intimidator: "Cowards can't block Warriors.").
         * {@link BlockDenial#detail()} is the restriction's own sentence, surfaced verbatim.
         */
        GLOBAL_RESTRICTION,

        /**
         * The attacker can be blocked only by creatures matching a filter the blocker fails — whether
         * printed on the attacker, granted by an aura attached to it, or imposed until end of turn.
         * {@link BlockDenial#detail()} is the phrase describing the allowed blockers.
         */
        ATTACKER_LIMITED_TO_BLOCKERS,

        /**
         * The attacker can't be blocked by creatures matching a filter the blocker <em>does</em> match.
         * Where the restriction only applies while the defending player controls a given permanent,
         * that condition is met too.
         */
        CANT_BE_BLOCKED_BY_MATCHING,

        /**
         * The attacker can't be blocked by creatures with less power than its own (Shrill Howler), and
         * the blocker's effective power is lower.
         */
        CANT_BE_BLOCKED_BY_LESS_POWER,

        /**
         * CR 702.14b — the attacker has a landwalk keyword and the defending player controls a land of
         * that type. {@link BlockDenial#detail()} is the land type name, already lowercased for the
         * message ("island" → "islandwalk").
         */
        LANDWALK,

        /** A one-shot effect stopped this creature from blocking for the rest of the turn. */
        CANT_BLOCK_THIS_TURN,

        /**
         * The blocker can't block at all, for a reason independent of which attacker it faces: a
         * "can't block" static or aura, decayed, a board-wide "creatures matching X can't attack or
         * block" restriction, or a lock imposed for this combat.
         */
        CANT_BLOCK,

        /**
         * The blocker can't block attackers whose power is greater than or equal to its <em>own</em>
         * toughness (Ironclaw Curse), and this attacker's power reaches it.
         */
        CANT_BLOCK_POWER_AT_LEAST_OWN_TOUGHNESS,

        /**
         * The blocker can't block attackers whose power is greater than or equal to a fixed threshold
         * (Ironclaw Orcs: power 2 or greater), and this attacker's power reaches it.
         */
        CANT_BLOCK_HIGH_POWER,

        /**
         * An effect named this exact attacker when it stopped the blocker from blocking, so the block
         * is illegal against it alone (e.g. a "can't block that creature this turn" rider).
         */
        CANT_BLOCK_THAT_ATTACKER,

        /**
         * CR 702.16e — the <em>attacker</em> has protection from a quality of the blocker (its color,
         * card type, subtype, or mana value), so it can't be blocked by it.
         */
        PROTECTION
    }

    public static final BlockDenial CANT_BE_BLOCKED = new BlockDenial(Reason.CANT_BE_BLOCKED, null);
    public static final BlockDenial FLYING = new BlockDenial(Reason.FLYING, null);
    public static final BlockDenial HORSEMANSHIP = new BlockDenial(Reason.HORSEMANSHIP, null);
    public static final BlockDenial FEAR = new BlockDenial(Reason.FEAR, null);
    public static final BlockDenial INTIMIDATE = new BlockDenial(Reason.INTIMIDATE, null);
    public static final BlockDenial SKULK = new BlockDenial(Reason.SKULK, null);
    public static final BlockDenial SHADOW = new BlockDenial(Reason.SHADOW, null);
    public static final BlockDenial CANT_BE_BLOCKED_BY_MATCHING = new BlockDenial(Reason.CANT_BE_BLOCKED_BY_MATCHING, null);
    public static final BlockDenial CANT_BE_BLOCKED_BY_LESS_POWER =
            new BlockDenial(Reason.CANT_BE_BLOCKED_BY_LESS_POWER, null);
    public static final BlockDenial CANT_BLOCK_THIS_TURN = new BlockDenial(Reason.CANT_BLOCK_THIS_TURN, null);
    public static final BlockDenial CANT_BLOCK = new BlockDenial(Reason.CANT_BLOCK, null);
    public static final BlockDenial CANT_BLOCK_POWER_AT_LEAST_OWN_TOUGHNESS =
            new BlockDenial(Reason.CANT_BLOCK_POWER_AT_LEAST_OWN_TOUGHNESS, null);
    public static final BlockDenial CANT_BLOCK_HIGH_POWER =
            new BlockDenial(Reason.CANT_BLOCK_HIGH_POWER, null);
    public static final BlockDenial CANT_BLOCK_THAT_ATTACKER = new BlockDenial(Reason.CANT_BLOCK_THAT_ATTACKER, null);
    public static final BlockDenial PROTECTION = new BlockDenial(Reason.PROTECTION, null);
}
