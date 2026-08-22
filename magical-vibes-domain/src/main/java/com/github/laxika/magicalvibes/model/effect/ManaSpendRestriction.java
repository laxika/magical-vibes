package com.github.laxika.magicalvibes.model.effect;

/**
 * The CR 106.6 rider an {@link AwardAnyColorManaEffect} puts on the mana it produces — a restriction
 * on what the mana can be spent on, an additional effect on whatever it is spent on, or a delayed
 * triggered ability that fires when it is spent. None of these change the mana's type.
 *
 * <p>The constant also fixes <em>how</em> the colour is chosen. {@link #FLASHBACK_ONLY} and
 * {@link #SUBTYPE_SPELL_OR_ABILITY} are the "add N mana in any combination of colors" wordings, where
 * the controller picks a colour per mana; every other constant is "add N mana of any one color",
 * where a single pick colours the whole batch. That is why the axis is derived here rather than
 * carried as a separate component on the effect.
 */
public enum ManaSpendRestriction {

    /** No rider — "Add N mana of any one color" lands in the ordinary pool (Birds of Paradise). */
    NONE,

    /** Spendable only to pay activated ability costs (Thran Turbine). */
    ABILITIES,

    /** Choose from the colors of the source permanent's imprinted card (Chrome Mox). */
    IMPRINTED_CARD_COLORS,

    /**
     * Unrestricted mana that also registers the delayed trigger copying the instant or sorcery it
     * pays for (Primal Wellspring).
     */
    INSTANT_SORCERY_COPY,

    /** Spendable only to cast instant and sorcery spells (Resonating Lute). */
    INSTANT_SORCERY_ONLY,

    /** Spendable only to cast artifact spells or activate abilities of artifacts (Vedalken Engineer). */
    ARTIFACT_SPELLS_OR_ABILITIES,

    /** Spendable only to cast spells with flashback from a graveyard (Altar of the Lost). */
    FLASHBACK_ONLY,

    /** Spendable only to cast creature spells of any type (Ancient Ziggurat, Somberwald Sage). */
    CREATURE_SPELL_ONLY,
    CREATURE_OR_ENCHANTMENT_SPELL_ONLY,

    /** Spendable only to cast creature spells of the effect's printed subtype (The Seedcore). */
    SUBTYPE_CREATURE_SPELL,

    /** Spendable only to cast planeswalker spells (Interplanar Beacon). */
    PLANESWALKER_SPELLS,

    /** Spendable only to cast creature spells or activate abilities of creature sources (Gwenna, Eyes of Gaea). */
    CREATURE_SPELLS_OR_ABILITIES,

    /**
     * Spendable only to cast creature spells of the type chosen as the source entered
     * (Pillar of Origins, Unclaimed Territory).
     */
    CHOSEN_SUBTYPE_CREATURE,

    /**
     * Spendable only to cast spells of the source permanent's chosen creature subtype or activate
     * abilities of permanents of that subtype.
     */
    CHOSEN_SUBTYPE_SPELL_OR_ABILITY,

    /** Spendable only to cast creature spells of the source's chosen subtype or activate abilities
     * of creature sources of that subtype (Secluded Courtyard). */
    CHOSEN_SUBTYPE_CREATURE_SOURCE_SPELL_OR_ABILITY,

    /**
     * As {@link #CHOSEN_SUBTYPE_CREATURE}, and the spell it pays for can't be countered
     * (Cavern of Souls).
     */
    CHOSEN_SUBTYPE_CREATURE_UNCOUNTERABLE,

    /**
     * Spendable only to cast spells of the effect's own {@code subtype} (Sliver Hive). Routes into
     * the same spell-only bucket as {@link #CHOSEN_SUBTYPE_CREATURE}, so it cannot pay for activated
     * abilities either; the difference is that the subtype is printed on the card rather than chosen
     * as the source entered.
     */
    SUBTYPE_SPELL,

    /**
     * Spendable only to cast spells of the effect's own {@code subtype} or to activate abilities of
     * permanents of that subtype (Smokebraider, Primal Beyond).
     */
    SUBTYPE_SPELL_OR_ABILITY,

    /** Spendable only to cast Mount or Vehicle spells (Intrepid Stablemaster). */
    MOUNT_OR_VEHICLE_SPELL,

    /** Spendable only to cast spells with mana value 4 or greater (Ashling, Rimebound). */
    MANA_VALUE_AT_LEAST_FOUR,

    /**
     * Spendable only to cast Cleric, Rogue, Warrior, or Wizard spells or activate abilities of those
     * subtypes (Base Camp).
     */
    PARTY_SPELL_OR_ABILITY
}
