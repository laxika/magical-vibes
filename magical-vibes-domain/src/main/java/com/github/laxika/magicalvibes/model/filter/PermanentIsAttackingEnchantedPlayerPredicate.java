package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches creatures attacking the player enchanted by the source Aura. Attacks against that
 * player's planeswalker or battle do not match.
 */
public record PermanentIsAttackingEnchantedPlayerPredicate() implements PermanentPredicate {
}
