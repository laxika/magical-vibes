package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * A single "resolve an effect when this creature dies this turn" registration (Skeletonize,
 * Initiate of Blood). Stored in {@link GameData#creatureTriggeringEffectOnDeathThisTurn} keyed by
 * the dying creature's card ID.
 *
 * @param effect            the effect to resolve when the creature dies
 * @param controllerId      the player who will control the pushed triggered ability
 * @param sourceCard        the card that registered the trigger (used as the triggered ability's
 *                          source, e.g. for a created token's set code)
 * @param sourcePermanentId the permanent that registered the trigger, or {@code null} when the
 *                          registration came from a spell; effects that act on their own source
 *                          (flipping, transforming) read it
 */
public record DelayedEffectOnDeath(CardEffect effect, UUID controllerId, Card sourceCard, UUID sourcePermanentId) {
}
