package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered effect: "Whenever a source deals damage to this creature,
 * that source's controller gets a poison counter."
 *
 * <p>Registered on the card as a marker (damageSourceControllerId=null).
 * At trigger collection time, a new instance with the actual source controller
 * is created and placed on the stack.
 */
public record DamageSourceControllerGetsPoisonCounterEffect(UUID damageSourceControllerId) implements CardEffect {
}
