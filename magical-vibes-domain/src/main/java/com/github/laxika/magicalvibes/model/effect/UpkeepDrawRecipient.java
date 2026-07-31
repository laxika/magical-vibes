package com.github.laxika.magicalvibes.model.effect;

/**
 * Who draws when a {@link RegisterDrawCardsAtNextUpkeepEffect} resolves.
 */
public enum UpkeepDrawRecipient {

    /** The player resolving the effect (Blessed Wine). */
    CONTROLLER,

    /** The targeted player (Sapphire Charm). */
    TARGET_PLAYER,

    /**
     * The controller of the spell targeted by this stack entry (Arcane Denial). Read from the stack,
     * so the effect must resolve while that spell is still there — i.e. before any accompanying
     * counter effect.
     */
    TARGET_SPELL_CONTROLLER,

    /**
     * The owner of the graveyard the entry's targeted cards are in (Lodestone Bauble). Read from the
     * graveyards, so the effect must resolve while those cards are still there — i.e. before any
     * accompanying effect that moves them out.
     */
    TARGET_GRAVEYARD_OWNER
}
