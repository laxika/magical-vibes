package com.github.laxika.magicalvibes.model.effect;

/**
 * "Each player may draw up to {@code maxDraw} cards. For each card less than {@code maxDraw} a player
 * draws this way, that player gains {@code lifePerCardBelow} life."
 * <p>
 * Resolution is a sequential, per-player interaction in APNAP order (active player first). Each player
 * in turn chooses how many cards to draw (0 to {@code maxDraw}); they then draw that many and gain
 * {@code lifePerCardBelow} life for each card fewer than {@code maxDraw} they chose to draw. Used by
 * Temporary Truce ({@code maxDraw=2}, {@code lifePerCardBelow=2}).
 *
 * @param maxDraw          the maximum number of cards each player may draw
 * @param lifePerCardBelow life gained per card fewer than {@code maxDraw} drawn
 */
public record EachPlayerMayDrawUpToNGainLifePerCardBelowEffect(int maxDraw, int lifePerCardBelow)
        implements CardEffect {
}
