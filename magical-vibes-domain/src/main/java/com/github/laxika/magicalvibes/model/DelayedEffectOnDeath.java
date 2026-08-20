package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * A single "resolve an effect when this permanent dies this turn" registration. Stored in
 * {@link GameData#permanentTriggeringEffectOnDeathThisTurn} keyed by the dying permanent's card ID.
 *
 * @param effect            the effect to resolve when the permanent dies
 * @param controllerId      the player who will control the pushed triggered ability
 * @param sourceCard        the card that registered the trigger (used as the triggered ability's
 *                          source, e.g. for a created token's set code)
 * @param sourcePermanentId the permanent that registered the trigger, or {@code null} when the
 *                          registration came from a spell; effects that act on their own source
 *                          (flipping, transforming) read it
 */
public record DelayedEffectOnDeath(CardEffect effect, UUID controllerId, Card sourceCard, UUID sourcePermanentId) {
}
