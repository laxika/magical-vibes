package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * Delayed trigger created by revealing a card from opening hand (e.g. Chancellor cycle).
 * Fires once per opponent when they cast their first spell of the game.
 *
 * @param revealingPlayerId the player who revealed the card
 * @param sourceCard        the revealed card (e.g. Chancellor of the Annex)
 * @param effect            the effect to apply when the trigger fires
 * @param revealersFirstUpkeep for first-upkeep triggers: waits for the revealing player's own first upkeep
 */
public record OpeningHandRevealTrigger(UUID revealingPlayerId, Card sourceCard, CardEffect effect,
                                       boolean revealersFirstUpkeep) {

    public OpeningHandRevealTrigger(UUID revealingPlayerId, Card sourceCard, CardEffect effect) {
        this(revealingPlayerId, sourceCard, effect, false);
    }
}
