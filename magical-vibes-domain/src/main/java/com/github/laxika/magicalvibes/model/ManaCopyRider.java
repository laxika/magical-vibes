package com.github.laxika.magicalvibes.model;

/**
 * The "when this mana is spent to ..., copy that spell" rider a unit of mana can carry in a
 * {@link ManaPool}. Each unit is its own delayed trigger (CR 106.6a); it fires only for the spell
 * or ability the unit actually pays for, and only when that spell or ability matches the rider.
 */
public enum ManaCopyRider {
    /** "When you spend this mana to cast a spell or activate an ability, copy that spell or ability" (Sunken Palace). */
    SPELL_OR_ABILITY,
    /** "When that mana is spent to cast an instant or sorcery spell, copy that spell" (Primal Wellspring). */
    INSTANT_OR_SORCERY_SPELL,
    /** "When that mana is spent to cast a red instant or sorcery spell, copy that spell" (Pyromancer's Goggles). */
    RED_INSTANT_OR_SORCERY_SPELL
}
