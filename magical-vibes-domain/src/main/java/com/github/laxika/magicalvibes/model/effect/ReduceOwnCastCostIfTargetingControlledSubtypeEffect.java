package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Reduces this spell's casting cost by the given amount if it targets a permanent
 * with the specified subtype that the caster controls.
 * <p>
 * E.g. Savage Stomp costs {2} less if it targets a Dinosaur you control.
 * <p>
 * For the playability check, this optimistically assumes the player will target
 * a matching permanent (checking if they control one). The actual cost reduction
 * during casting is validated against the chosen first target.
 *
 * @param subtype the subtype the first target must have for the reduction
 * @param amount  the generic mana reduction
 */
public record ReduceOwnCastCostIfTargetingControlledSubtypeEffect(
        CardSubtype subtype,
        int amount
) implements CardEffect {
}
