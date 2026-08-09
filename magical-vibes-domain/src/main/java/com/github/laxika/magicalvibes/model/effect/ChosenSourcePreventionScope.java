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

    /**
     * "The next time it would deal damage to enchanted creature this turn, prevent that damage."
     * The protected permanent is the creature the ability's source Aura is attached to, read from
     * the last-known snapshot so a sacrifice cost doesn't lose it (Kithkin Armor).
     */
    NEXT_DAMAGE_TO_ENCHANTED,

    /** "The next time it would deal damage to target creature this turn, prevent that damage." */
    NEXT_DAMAGE_TO_TARGET_CREATURE,

    /** "Prevent all damage it would deal this turn" (to you only, or to anything). */
    ALL_DAMAGE_THIS_TURN
}
