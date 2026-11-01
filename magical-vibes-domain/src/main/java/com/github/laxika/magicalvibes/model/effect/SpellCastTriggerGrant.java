package com.github.laxika.magicalvibes.model.effect;

/**
 * Which of its controller's spells a {@link GrantCastTriggerToSpellsEffect} gives its triggered keyword to.
 * {@code TriggerCollectionService.checkSpellCastTriggers} checks each kind as a spell is cast.
 */
public enum SpellCastTriggerGrant {
    /** "Instant and sorcery spells you cast have storm." (Prismari, the Inspiration) */
    STORM_TO_INSTANT_OR_SORCERY,
    /** "The first spell you cast each turn has cascade." (Maelstrom Nexus) */
    CASCADE_TO_FIRST_SPELL,
    /** "The first spell you cast each turn that mana from a Treasure was spent to cast has cascade." (Rain of Riches) */
    CASCADE_TO_FIRST_SPELL_USING_TREASURE_MANA,
    /** "Instant and sorcery spells you cast from your hand have cascade." (Quandrix, the Proof) */
    CASCADE_TO_INSTANT_OR_SORCERY_FROM_HAND,
    /** "Enchantment spells you cast from your hand have cascade." (Wildsear, Scouring Maw) */
    CASCADE_TO_ENCHANTMENT_FROM_HAND,
    /** "Sliver spells you cast have cascade." (The First Sliver) */
    CASCADE_TO_SLIVER_SPELL,
    /** "The first spell you cast from anywhere other than your hand each turn has demonstrate." (The Twelfth Doctor) */
    DEMONSTRATE_TO_FIRST_SPELL_FROM_OUTSIDE_HAND,
    /**
     * "During your turn, spells you cast from your hand with mana value X or less have cascade, where X is the
     * total amount of life your opponents have lost this turn." (Abaddon the Despoiler)
     */
    CASCADE_TO_HAND_SPELLS_WITH_MANA_VALUE_AT_MOST_OPPONENTS_LIFE_LOST
}
