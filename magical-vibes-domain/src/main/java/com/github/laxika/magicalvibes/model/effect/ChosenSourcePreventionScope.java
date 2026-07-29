package com.github.laxika.magicalvibes.model.effect;

/**
 * What a {@link PreventDamageFromChosenSourceEffect} shield covers once the source is chosen.
 */
public enum ChosenSourcePreventionScope {

    /** "The next time it would deal damage to you this turn, prevent that damage." */
    NEXT_DAMAGE_TO_CONTROLLER,

    /** "The next time it would deal damage to any target this turn, prevent that damage." */
    NEXT_DAMAGE_TO_ANY_TARGET,

    /**
     * "The next time it would deal damage to you and/or creatures you control this turn, prevent
     * that damage. If damage from a black source is prevented this way, you gain that much life."
     * (Shadowbane).
     */
    NEXT_DAMAGE_TO_CONTROLLER_AND_CREATURES,

    /** "Prevent all damage it would deal this turn" (to you only, or to anything). */
    ALL_DAMAGE_THIS_TURN
}
