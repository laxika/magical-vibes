package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Controller-scoped triggered mana ability: whenever the controller taps a creature for mana,
 * they add one additional mana of {@code color}.
 */
public record AddManaWhenCreatureTappedForManaEffect(ManaColor color) implements CardEffect {
}
