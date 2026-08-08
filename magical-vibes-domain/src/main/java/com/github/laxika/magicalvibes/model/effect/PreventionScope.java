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
    /** "Prevent the next N damage that would be dealt to enchanted creature this turn" — shields the Aura's attached permanent (Fylgja). */
    NEXT_TO_ENCHANTED,
    /** "Prevent the next N damage that would be dealt to any target" with a chosen target creature/player (Healing Salve). */
    NEXT_TO_TARGET,
    /** "Prevent the next N damage that would be dealt to target creature this turn" (Soldevi Heretic).
     *  Resolves exactly like {@link #NEXT_TO_TARGET}; the separate scope exists so the effect can
     *  declare a creature-only {@code TargetSpec} and a player can never be chosen. */
    NEXT_TO_TARGET_CREATURE,
    /** "Prevent the next N damage that would be dealt to target player or planeswalker this turn" (Wandering Mage).
     *  Resolves exactly like {@link #NEXT_TO_TARGET}; the separate scope exists so the effect can
     *  declare a player-or-planeswalker {@code TargetSpec} and a creature can never be chosen. */
    NEXT_TO_TARGET_PLAYER_OR_PLANESWALKER,
    /** "Prevent the next N damage that would be dealt to each creature and each player this turn" (Kitsune Palliator).
     *  Non-targeting: every creature on the battlefield as the ability resolves gets its own next-N
     *  shield and every player gets one too; creatures entering later are unaffected. */
    NEXT_TO_EACH_CREATURE_AND_PLAYER,
    /** "Prevent all combat damage that would be dealt this turn" (Fog, Holy Day). */
    ALL_COMBAT,
    /** "Prevent all damage that would be dealt to creatures this turn" (Blinding Fog). */
    ALL_TO_CREATURES,
    /** "Prevent all damage that would be dealt to [permanents matching a predicate] this turn" (Ethersworn Shieldmage). */
    ALL_TO_MATCHING_PERMANENTS,
    /** "Prevent all [combat] damage that would be dealt to target creature(s) this turn" (Foxfire, Redeem). */
    ALL_TO_TARGET_CREATURES,
    /** "Prevent all [combat] damage target creature(s) would deal this turn" (Soul Parry, Resistance Fighter). */
    ALL_BY_TARGET_CREATURES,
    /** "Until your next turn, prevent all damage target permanent would deal" (Gideon of the Trials +1).
     *  Unlike {@link #ALL_BY_TARGET_CREATURES} this targets any permanent and lasts until the
     *  controller's next turn rather than only the current turn. */
    ALL_BY_TARGET_PERMANENT_UNTIL_NEXT_TURN,
    /** "Prevent all damage that would be dealt to ~ this turn" — the source permanent
     *  (Gideon of the Trials 0). */
    ALL_TO_SELF,
    /** "Prevent all [combat] damage that would be dealt by ~ this turn" — the source permanent
     *  (Goblin Snowman). The by-side counterpart of {@link #ALL_TO_SELF}. */
    ALL_BY_SELF,
    /** "Prevent all damage that would be dealt to you and creatures you control this turn" (Safe Passage). */
    ALL_TO_CONTROLLER_AND_CREATURES,
    /** "Prevent all damage that would be dealt to you this turn" (Riot Control). Unlike
     *  {@link #ALL_TO_CONTROLLER_AND_CREATURES} the controller's creatures are not shielded. */
    ALL_TO_CONTROLLER,
    /** "Prevent all damage attacking creatures would deal to you this turn" (Deep Wood). */
    ALL_TO_CONTROLLER_FROM_ATTACKERS,
    /** "Prevent all damage that sources of the chosen colors would deal this turn" (Luminesce). */
    ALL_FROM_COLORS,
    /** "Prevent all combat damage this turn except that dealt by [exempt] creatures" (Moonmist). */
    ALL_COMBAT_EXCEPT,
    /** "Prevent all combat damage that would be dealt by creatures other than target creature this turn"
     *  (Terrifying Presence). The target-chosen counterpart of {@link #ALL_COMBAT_EXCEPT}: the exemption
     *  predicate is built at resolution from the chosen target. */
    ALL_COMBAT_EXCEPT_TARGET,
    /** "Prevent all damage that would be dealt by creatures this turn" (Ethereal Haze). */
    ALL_BY_CREATURES
}
