package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability for static effects that impose an additional "sacrifice a matching permanent" cost
 * for each mana symbol of a given color in a spell's mana cost and/or an activated ability's
 * activation cost (e.g. Drought: sacrifice a Swamp per black mana symbol). Symmetric — applies
 * regardless of who controls the taxing permanent. Collected by {@code CastingCostService}
 * without naming the concrete effect type.
 */
public interface AdditionalSacrificePerManaSymbolTaxEffect extends CardEffect {

    /** Color of mana symbols that each impose one additional sacrifice. */
    ManaColor color();

    /** Permanents the caster/activator must sacrifice (one per matching mana symbol). */
    PermanentPredicate sacrificeFilter();

    /** Human-readable description of one sacrifice (e.g. "a Swamp"). */
    String description();

    /** Whether this tax applies when casting spells. */
    boolean taxesSpells();

    /** Whether this tax applies when activating abilities. */
    boolean taxesActivatedAbilities();
}
