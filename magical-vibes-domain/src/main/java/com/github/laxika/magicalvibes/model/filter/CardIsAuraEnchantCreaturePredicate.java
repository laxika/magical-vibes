package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches Aura cards whose enchant ability restricts them to creatures ("enchant creature",
 * "enchant creature you control", …). An Aura's enchant restriction is modelled as the card's
 * spell target filter, so this reads that filter rather than any separate metadata. Rootwater
 * Shaman.
 */
public record CardIsAuraEnchantCreaturePredicate() implements CardPredicate {
}
