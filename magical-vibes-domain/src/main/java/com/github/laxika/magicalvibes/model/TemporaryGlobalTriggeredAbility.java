package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * A global triggered ability registered for the rest of the turn by a resolving effect.
 * The source card is retained because the spell that registered the ability is no longer on the
 * battlefield when the trigger fires.
 */
public record TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, EffectSlot slot,
                                              CardEffect effect) {
}
