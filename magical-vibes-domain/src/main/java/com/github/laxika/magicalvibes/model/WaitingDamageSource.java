package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * The card chosen as the damage source of a prevention or redirection shield that is still waiting to apply, as the
 * floating effect that carries it (CR 611.2a) lasts until end of turn with the shield. Keyed by the id the shield
 * stores, it lets the source be chosen again after it has left the battlefield (CR 609.7a).
 *
 * @param sourceId the id the shield stores for the chosen source
 * @param card     the chosen source's card
 */
public record WaitingDamageSource(UUID sourceId, Card card) implements CardEffect {
}
