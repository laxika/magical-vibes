package com.github.laxika.magicalvibes.model.amount;

/**
 * Evaluates to {@code amount} when the controller controls strictly more creatures than every other
 * player, and to {@code otherwise} when any other player controls at least as many.
 *
 * <p>Models "If you control more creatures than each other player, … {amount} …. Otherwise, …
 * {otherwise} …" (Advice from the Fae). Fed as the {@code chooseCount} of a
 * {@code LookAtTopCardsEffect} so the number of cards kept scales with the creature-count check at
 * resolution time.
 */
public record FixedIfControlMoreCreaturesThanEachOtherPlayer(int amount, int otherwise)
        implements DynamicAmount {
}
