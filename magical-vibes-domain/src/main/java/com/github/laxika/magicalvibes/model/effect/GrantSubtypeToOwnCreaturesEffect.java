package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** One-shot effect that permanently grants a subtype to the controller's creatures. */
public record GrantSubtypeToOwnCreaturesEffect(CardSubtype subtype) implements CardEffect {
}
