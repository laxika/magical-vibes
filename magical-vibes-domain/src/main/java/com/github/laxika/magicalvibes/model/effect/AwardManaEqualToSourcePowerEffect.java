package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Adds mana of the specified color equal to the source permanent's effective power.
 * Implements {@link ManaProducingEffect} so the engine treats the ability as a mana ability (CR 605.1a).
 *
 * <p>Used by cards like Marwyn, the Nurturer ("{T}: Add an amount of {G} equal to Marwyn's power.").
 */
public record AwardManaEqualToSourcePowerEffect(ManaColor color) implements ManaProducingEffect {
}
