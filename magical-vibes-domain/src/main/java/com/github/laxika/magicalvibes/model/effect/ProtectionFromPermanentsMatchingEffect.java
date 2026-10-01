package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Protection from matching battlefield permanents, excluding spells with the same characteristics. */
public record ProtectionFromPermanentsMatchingEffect(PermanentPredicate predicate) implements CardEffect {
}
