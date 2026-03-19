package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Reveals cards from the top of the defending player's library until a card matching one of the
 * specified types is found. The equipped creature gets +powerBoostPerCard/+toughnessBoostPerCard
 * until end of turn for each card revealed this way. All revealed cards (including the matching
 * card) are put into that player's graveyard.
 * <p>
 * Used on equipment with ON_ATTACK triggers. The defending player is derived as the opponent
 * of the controller. The equipped creature is found via the source equipment's attachedTo.
 * <p>
 * If the player's library contains no matching card, their entire library is revealed,
 * put into the graveyard, and the creature gets the boost for all cards revealed.
 * <p>
 * Used by Trepanation Blade (cardTypes = {LAND}, powerBoostPerCard = 1, toughnessBoostPerCard = 0).
 */
public record RevealUntilTypeMillAndBoostAttackerEffect(
        Set<CardType> cardTypes,
        int powerBoostPerCard,
        int toughnessBoostPerCard
) implements CardEffect {
}
