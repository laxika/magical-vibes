package com.github.laxika.magicalvibes.model.effect;

/**
 * Mana ability effect that adds X mana of any one color, where X is the ability's xValue (e.g. the
 * number of permanents sacrificed for a {@link SacrificeXPermanentsCost}). The X-scaled sibling of
 * {@link AwardAnyColorManaEffect}: the player chooses a single color and receives X mana of it.
 *
 * <p>Example: "Add X mana of any one color" on Springjack Pasture's sacrifice ability. Resolved
 * immediately as a mana ability; a companion "you gain X life" rider is a separate
 * {@link GainLifeEffect} in the same ability.
 */
public record AwardXAnyColorManaEffect() implements ManaProducingEffect {
}
