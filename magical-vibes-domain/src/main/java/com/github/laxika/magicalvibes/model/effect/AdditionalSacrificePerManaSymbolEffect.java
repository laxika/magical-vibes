package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Spells and/or activated abilities cost an additional "Sacrifice a [matching permanent]" for each
 * mana symbol of {@code color} in their mana / activation cost. Static, symmetric (Drought:
 * {@code BLACK} + Swamp filter, both tax flags true). Hybrid/Phyrexian symbols of that color each
 * count once via {@code ManaCost.countColorSymbols}.
 */
public record AdditionalSacrificePerManaSymbolEffect(
        ManaColor color,
        PermanentPredicate sacrificeFilter,
        String description,
        boolean taxesSpells,
        boolean taxesActivatedAbilities
) implements AdditionalSacrificePerManaSymbolTaxEffect {
}
