package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaCopyRider;

/**
 * Mana-ability rider "when this mana is spent to cast [a matching spell], copy that spell"
 * (Sunken Palace, Pyromancer's Goggles). Pair it with the mana effect of the same ability: every
 * unit of mana the ability produces is tagged in the {@code ManaPool} with {@code rider}, and the
 * spell (or, for {@link ManaCopyRider#SPELL_OR_ABILITY}, non-mana ability) a tagged unit actually
 * pays for is copied once per unit spent when it matches the rider (CR 106.6a). Units spent on
 * anything that doesn't match do nothing.
 */
public record ManaCopyRiderEffect(ManaCopyRider rider) implements CardEffect {
}
