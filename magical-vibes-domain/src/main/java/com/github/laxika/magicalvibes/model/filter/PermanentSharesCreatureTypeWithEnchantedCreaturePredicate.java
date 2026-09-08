package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents that share a creature type with the creature enchanted by the source Aura.
 * Changeling counts as every creature type, and an unattached Aura matches nothing.
 */
public record PermanentSharesCreatureTypeWithEnchantedCreaturePredicate() implements PermanentPredicate {
}
