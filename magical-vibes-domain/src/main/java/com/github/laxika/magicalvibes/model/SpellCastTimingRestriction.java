package com.github.laxika.magicalvibes.model;

/**
 * A card-specific restriction on <em>when</em> a spell may be cast, beyond the normal
 * instant/sorcery-speed timing rules. Enforced by {@code CastingPermissionService} and surfaced
 * through the playable-card computation in {@code GameActionAvailabilityService}.
 */
public enum SpellCastTimingRestriction {
    /**
     * "Cast this spell only during the declare attackers step." Teleport.
     */
    DECLARE_ATTACKERS,

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
     * "Cast this spell only during combat."
     */
    COMBAT,

    /**
     * "Cast this spell only during combat on your turn."
     */
    YOUR_COMBAT,

    /**
     * "Cast this spell only during combat on your turn before blockers are declared." Melee. As
     * {@code COMBAT_BEFORE_BLOCKERS}, but additionally requires the caster to be the active player.
     */
    YOUR_COMBAT_BEFORE_BLOCKERS,

    /**
     * "Cast this spell only during combat after blockers are declared." Aleatory. Legal during the
     * declare-blockers, combat-damage and end-of-combat steps of any player's combat.
     */
    COMBAT_AFTER_BLOCKERS,

    /**
     * "Cast this spell only during combat." Cauldron Dance.
     */
    ONLY_DURING_COMBAT,

    /**
     * "Cast this spell only during the declare blockers step." Dazzling Beauty. Legal for any
     * player during any player's declare-blockers step.
     */
    DECLARE_BLOCKERS,

    /**
     * "Cast this spell only during an opponent's turn, before attackers are declared." Siren's Call.
     * Legal only when the caster is not the active player and the current step precedes the declare
     * attackers step (any step of the beginning, precombat main, or beginning-of-combat).
     */
    OPPONENTS_TURN_BEFORE_ATTACKERS,

    /**
     * "Cast this spell only during an opponent's turn." Delirium. Legal in any step of a turn in
     * which the caster is not the active player.
     */
    OPPONENTS_TURN,

    /**
     * "Cast this spell only during an opponent's upkeep." Festival.
     */
    OPPONENTS_UPKEEP,

    /**
     * "Cast this spell only before the combat damage step." Blood Frenzy. Legal for any player in
     * any step that precedes the combat damage step of the current turn, so it covers the beginning
     * phase, the precombat main phase and the first three combat steps, but not the combat damage
     * step or anything after it.
     */
    BEFORE_COMBAT_DAMAGE,

    /**
     * "Cast this spell only after combat." Jabari's Influence. Legal for any player once the combat
     * phase has ended, i.e. during the postcombat main phase or the ending phase of any turn.
     */
    AFTER_COMBAT
}
