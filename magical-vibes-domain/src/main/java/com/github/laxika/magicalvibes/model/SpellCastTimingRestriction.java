package com.github.laxika.magicalvibes.model;

/**
 * A card-specific restriction on <em>when</em> a spell may be cast, beyond the normal
 * instant/sorcery-speed timing rules. Enforced by {@code CastingPermissionService} and surfaced
 * through the playable-card computation in {@code GameBroadcastService}.
 */
public enum SpellCastTimingRestriction {
    /**
     * "Cast this spell only during the declare attackers step and only if you've been attacked
     * this step." Defiant Stand.
     */
    DECLARE_ATTACKERS_IF_ATTACKED,

    /**
     * "Cast this spell only during your end step." Necrologia.
     */
    YOUR_END_STEP,

    /**
     * "Cast this spell only during combat before blockers are declared." Panic. Legal during the
     * beginning-of-combat and declare-attackers steps (any player's combat), not once the
     * declare-blockers step has begun.
     */
    COMBAT_BEFORE_BLOCKERS,

    /**
     * "Cast this spell only during an opponent's turn, before attackers are declared." Siren's Call.
     * Legal only when the caster is not the active player and the current step precedes the declare
     * attackers step (any step of the beginning, precombat main, or beginning-of-combat).
     */
    OPPONENTS_TURN_BEFORE_ATTACKERS
}
