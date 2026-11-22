package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

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
 * @param players             who may draw ("each other player may draw up to three cards", Indentured Djinn); never a
 *                            target
 */
public record EachPlayerMayDrawUpToNGainLifePerCardBelowEffect(int maxDraw, int lifePerCardBelow,
                                                                boolean drawAfterEachChoice, PlayerSubject players)
        implements CardDrawingEffect {

    public EachPlayerMayDrawUpToNGainLifePerCardBelowEffect {
        if (drawAfterEachChoice && lifePerCardBelow != 0) {
            throw new IllegalArgumentException("drawAfterEachChoice does not support life gain");
        }
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("The players who may draw are not targeted: " + players);
        }
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(maxDraw);
    }

    @Override
    public PlayerSubject drawingPlayer() {
        return players;
    }

    /** "[Players] may each draw up to {@code maxDraw} cards", each choosing and drawing in turn. */
    public static EachPlayerMayDrawUpToNGainLifePerCardBelowEffect drawUpToInTurnOrder(int maxDraw,
                                                                                       PlayerSubject players) {
        return new EachPlayerMayDrawUpToNGainLifePerCardBelowEffect(maxDraw, 0, true, players);
    }

    public EachPlayerMayDrawUpToNGainLifePerCardBelowEffect(int maxDraw, int lifePerCardBelow) {
        this(maxDraw, lifePerCardBelow, false, Subjects.eachPlayer());
    }

    /** "Each player draws up to {@code maxDraw} cards", each choosing and drawing in turn order. */
    public static EachPlayerMayDrawUpToNGainLifePerCardBelowEffect drawUpToInTurnOrder(int maxDraw) {
        return drawUpToInTurnOrder(maxDraw, Subjects.eachPlayer());
    }
}
