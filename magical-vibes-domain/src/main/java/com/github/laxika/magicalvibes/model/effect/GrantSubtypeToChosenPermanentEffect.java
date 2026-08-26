package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Permanently grants a subtype to the permanent chosen earlier during the same resolution.
 * The chosen permanent is stored on the resolving stack entry.
 */
public record GrantSubtypeToChosenPermanentEffect(CardSubtype subtype) implements CardEffect {
}
