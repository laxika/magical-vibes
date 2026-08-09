package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Removes all land subtypes from matching lands without changing their other card types. */
public record LoseAllLandTypesEffect(GrantScope scope, PermanentPredicate filter) implements CardEffect {
}
