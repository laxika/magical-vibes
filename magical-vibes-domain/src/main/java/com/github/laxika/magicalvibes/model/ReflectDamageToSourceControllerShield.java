package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * "The next time a source of your choice would deal damage this turn, that source deals that damage to its controller
 * instead" (CR 614, CR 615): the chosen source, as the floating effect that carries it (CR 611.2a) holds it.
 *
 * @param sourcePermanentId the id the chosen source goes by
 */
public record ReflectDamageToSourceControllerShield(UUID sourcePermanentId) implements CardEffect {
}
