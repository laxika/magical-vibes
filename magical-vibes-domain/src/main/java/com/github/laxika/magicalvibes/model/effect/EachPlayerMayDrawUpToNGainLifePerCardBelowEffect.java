package com.github.laxika.magicalvibes.model.effect;

/**
 * "Each player may draw up to {@code maxDraw} cards. For each card less than {@code maxDraw} a player
 * draws this way, that player gains {@code lifePerCardBelow} life."
 * <p>
 * Resolution has two steps that involve every player (CR 608.2e): in APNAP order each player chooses how
 * many cards to draw (0 to {@code maxDraw}), then the players draw; each player then gains
 * {@code lifePerCardBelow} life for each card fewer than {@code maxDraw} they drew this way. Used by
 * Temporary Truce ({@code maxDraw=2}, {@code lifePerCardBelow=2}).
 * <p>
 * With {@code drawAfterEachChoice}, each player instead draws right after making their choice, before the
 * next player in turn order chooses (the official Diminishing Returns ruling: each player chooses a number
 * and draws, then the next player does the same). That mode grants no life.
 *
 * @param maxDraw          the maximum number of cards each player may draw
 * @param lifePerCardBelow    life gained per card fewer than {@code maxDraw} drawn
 * @param drawAfterEachChoice whether each player draws immediately after choosing, before the next choice
 */
public record EachPlayerMayDrawUpToNGainLifePerCardBelowEffect(int maxDraw, int lifePerCardBelow,
                                                                boolean drawAfterEachChoice)
        implements CardEffect {

    public EachPlayerMayDrawUpToNGainLifePerCardBelowEffect {
        if (drawAfterEachChoice && lifePerCardBelow != 0) {
            throw new IllegalArgumentException("drawAfterEachChoice does not support life gain");
        }
    }

    public EachPlayerMayDrawUpToNGainLifePerCardBelowEffect(int maxDraw, int lifePerCardBelow) {
        this(maxDraw, lifePerCardBelow, false);
    }

    /** "Each player draws up to {@code maxDraw} cards", each choosing and drawing in turn order. */
    public static EachPlayerMayDrawUpToNGainLifePerCardBelowEffect drawUpToInTurnOrder(int maxDraw) {
        return new EachPlayerMayDrawUpToNGainLifePerCardBelowEffect(maxDraw, 0, true);
    }
}
