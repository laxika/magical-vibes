package com.github.laxika.magicalvibes.model.effect;

/**
 * Which shield-state slot a resolving {@link PreventDamageEffect} writes. {@code NEXT_*} scopes
 * add an amount shield consumed as damage is dealt; {@code ALL_*} scopes set an
 * until-end-of-turn full-prevention flag.
 */
public enum PreventionScope {
    /** "Prevent the next N damage that would be dealt to any target" — the global shield (Samite Healer). */
    NEXT_TO_ANY,
    /** "Prevent the next N damage that would be dealt to you" (Esper Battlemage). */
    NEXT_TO_CONTROLLER,
    /** "Prevent the next N damage that would be dealt to ~" — the source permanent's own shield (Ethereal Champion). */
    NEXT_TO_SELF,
    /** "Prevent the next N damage that would be dealt to any target" with a chosen target creature/player (Healing Salve). */
    NEXT_TO_TARGET,
    /** "Prevent all combat damage that would be dealt this turn" (Fog, Holy Day). */
    ALL_COMBAT,
    /** "Prevent all damage that would be dealt to creatures this turn" (Blinding Fog). */
    ALL_TO_CREATURES,
    /** "Prevent all [combat] damage that would be dealt to target creature(s) this turn" (Foxfire, Redeem). */
    ALL_TO_TARGET_CREATURES,
    /** "Prevent all [combat] damage target creature(s) would deal this turn" (Soul Parry, Resistance Fighter). */
    ALL_BY_TARGET_CREATURES,
    /** "Prevent all damage that would be dealt to you and creatures you control this turn" (Safe Passage). */
    ALL_TO_CONTROLLER_AND_CREATURES,
    /** "Prevent all damage attacking creatures would deal to you this turn" (Deep Wood). */
    ALL_TO_CONTROLLER_FROM_ATTACKERS,
    /** "Prevent all damage that sources of the chosen colors would deal this turn" (Luminesce). */
    ALL_FROM_COLORS,
    /** "Prevent all combat damage this turn except that dealt by [exempt] creatures" (Moonmist). */
    ALL_COMBAT_EXCEPT
}
