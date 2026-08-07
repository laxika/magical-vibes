package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target opponent mills {@code count} cards. If two nonland cards that share a color were milled
 * this way, repeat this process." (Sphinx's Tutelage)
 *
 * <p>Declares {@link TargetSpec#NONE}: this engine is two-player, so the opponent is derived from
 * the resolving controller rather than chosen — the same convention as
 * {@link TargetOpponentMayDrawCardEffect}. That also matters here because the card's trigger sits in
 * {@code ON_CONTROLLER_DRAWS}, a slot with no player-targeting pipeline.
 */
public record MillOpponentRepeatOnSharedColorEffect(int count) implements CardEffect {
}
