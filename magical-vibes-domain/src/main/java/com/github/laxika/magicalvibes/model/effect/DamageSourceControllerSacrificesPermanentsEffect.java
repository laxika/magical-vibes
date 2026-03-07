package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered effect: "Whenever a source deals damage to this creature,
 * that source's controller sacrifices that many permanents."
 *
 * <p>Registered on the card as a marker (count=0, sacrificingPlayerId=null).
 * At trigger collection time, a new instance with actual values is created
 * and placed on the stack.
 */
public record DamageSourceControllerSacrificesPermanentsEffect(int count, UUID sacrificingPlayerId) implements CardEffect {
}
