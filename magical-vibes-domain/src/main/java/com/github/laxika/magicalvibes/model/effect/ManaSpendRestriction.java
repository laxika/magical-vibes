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

    /**
     * Unrestricted mana that also registers the delayed trigger copying the instant or sorcery it
     * pays for (Primal Wellspring).
     */
    INSTANT_SORCERY_COPY,

    /** Spendable only to cast instant and sorcery spells (Resonating Lute). */
    INSTANT_SORCERY_ONLY,

    /** Spendable only to cast spells with flashback from a graveyard (Altar of the Lost). */
    FLASHBACK_ONLY,

    /** Spendable only to cast creature spells of any type (Ancient Ziggurat, Somberwald Sage). */
    CREATURE_SPELL_ONLY,

    /**
     * Spendable only to cast creature spells of the type chosen as the source entered
     * (Pillar of Origins, Unclaimed Territory).
     */
    CHOSEN_SUBTYPE_CREATURE,

    /**
     * As {@link #CHOSEN_SUBTYPE_CREATURE}, and the spell it pays for can't be countered
     * (Cavern of Souls).
     */
    CHOSEN_SUBTYPE_CREATURE_UNCOUNTERABLE,

    /**
     * Spendable only to cast spells of the effect's own {@code subtype} or to activate abilities of
     * permanents of that subtype (Smokebraider, Primal Beyond).
     */
    SUBTYPE_SPELL_OR_ABILITY
}
