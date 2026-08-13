package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for static effects that can make a player's next draw impossible based on how many
 * cards that player has actually drawn this turn.
 */
public interface DrawRestrictionEffect extends CardEffect {

    boolean preventsDraw(int cardsDrawnThisTurn);
}
