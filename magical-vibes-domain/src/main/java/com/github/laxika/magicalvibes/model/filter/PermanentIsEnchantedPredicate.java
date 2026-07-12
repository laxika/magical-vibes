package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent that is enchanted — it has at least one Aura attached to it (regardless
 * of who controls the Aura). Requires game data to evaluate (the battlefield is scanned for
 * attached Auras). Used by Greater Auramancy ("Enchanted creatures you control have shroud").
 */
public record PermanentIsEnchantedPredicate() implements PermanentPredicate {
}
