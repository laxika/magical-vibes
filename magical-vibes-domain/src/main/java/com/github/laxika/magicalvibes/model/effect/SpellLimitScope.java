package com.github.laxika.magicalvibes.model.effect;

/**
 * Determines which players a {@link LimitSpellsPerTurnEffect} restricts.
 */
public enum SpellLimitScope {

    /**
     * Limits every player, regardless of who controls the permanent
     * (e.g. Rule of Law: "Each player can't cast more than one spell each turn.").
     */
    EACH_PLAYER,

    /**
     * Limits only the controller of the permanent carrying this effect
     * (e.g. Colfenor's Plans: "You can't cast more than one spell each turn.").
     */
    CONTROLLER,

    /**
     * Limits only the player enchanted by the Aura carrying this effect
     * (e.g. Curse of Exhaustion: "Enchanted player can't cast more than one spell each turn.").
     */
    ENCHANTED_PLAYER
}
