package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Lets a specified player choose a permanent matching {@code filter} and attaches the source Aura
 * to it. The choice is made while the effect resolves and is not a target choice.
 */
public record AttachSourceAuraToChosenPermanentEffect(PermanentPredicate filter) implements CardEffect {
}
