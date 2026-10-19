package com.github.laxika.magicalvibes.model.effect;

/**
 * "Each player may draw up to {@code maxDraw} cards. For each card less than {@code maxDraw} a player
 * draws this way, that player gains {@code lifePerCardBelow} life."
 * <p>
 * Resolution has two steps that involve every player (CR 608.2e): in APNAP order each player chooses how
 * many cards to draw (0 to {@code maxDraw}), then the players draw; each player then gains
 * {@code lifePerCardBelow} life for each card fewer than {@code maxDraw} they drew this way. Used by
 * Temporary Truce ({@code maxDraw=2}, {@code lifePerCardBelow=2}).
 *
 * @param maxDraw          the maximum number of cards each player may draw
 * @param lifePerCardBelow life gained per card fewer than {@code maxDraw} drawn
 */
public record EachPlayerMayDrawUpToNGainLifePerCardBelowEffect(int maxDraw, int lifePerCardBelow)
        implements CardEffect {
}
