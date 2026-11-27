package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target opponent mills {@code count} cards. If two nonland cards that share a color were milled
 * this way, repeat this process." (Sphinx's Tutelage)
 *
 * <p>Declares {@link TargetSpec#NONE}: the opponent is derived from the resolving controller rather than chosen,
 * because the card's trigger sits in a {@code Triggers.youDrawCard()} ability, whose collector has no
 * player-targeting pipeline.
 */
public record MillOpponentRepeatOnSharedColorEffect(int count) implements CardEffect {
}
