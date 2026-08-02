package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player reveals the top card of their library, loses life equal to that card's mana value,
 * then puts it into their hand.
 *
 * <p>Resolves in APNAP order; a player with an empty library simply does nothing.
 *
 * <p>Used by Duskmantle Seer (upkeep trigger).
 */
public record EachPlayerRevealsTopCardLosesLifeEqualToManaValueThenToHandEffect() implements CardEffect {
}
