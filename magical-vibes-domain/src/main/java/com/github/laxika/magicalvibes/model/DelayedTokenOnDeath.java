package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.UUID;

/**
 * A single "create token(s) when this creature dies this turn" registration (Skeletonize).
 * Stored in {@link GameData#creatureCreatingTokenOnDeathThisTurn} keyed by the dying creature's
 * card ID.
 *
 * @param tokenEffect  the token(s) to create when the creature dies
 * @param controllerId the player who will control the created token(s)
 * @param sourceCard   the card that registered the trigger (used as the triggered ability's source,
 *                     e.g. for the token's set code)
 */
public record DelayedTokenOnDeath(CreateTokenEffect tokenEffect, UUID controllerId, Card sourceCard) {
}
