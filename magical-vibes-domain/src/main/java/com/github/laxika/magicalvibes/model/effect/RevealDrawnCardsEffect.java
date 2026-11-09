package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Static ability: a player reveals the cards they draw as they draw them (CR 701.20a), detected by {@code DrawService}
 * as each card is drawn. "Reveal the first card you draw each turn" (Rowen, Primitive Etchings), "on each of your
 * turns" (Keranos, God of Storms), "you may reveal the first card you draw each turn as you draw it" (God-Eternal
 * Kefnet, Inquisitor Eisenhorn: the choice is made during the draw, CR 121.9, and pauses a multi-card draw), "the
 * chosen player reveals each card they draw" (Booby Trap), "players reveal each card they draw" (Sea of Sand, from the
 * command zone).
 * <p>
 * A card revealed this way is the event of the source's own "whenever you reveal a [card] this way" abilities
 * ({@code Triggers.youRevealCardThisWay()}, with the revealed card's filter), which the draw service collects for the
 * source that revealed it, the revealed card the stack entry's triggering card.
 *
 * @param revealer          who reveals the cards they draw
 * @param firstCardEachTurn only the player's first card drawn each turn (the draw count includes the draws before
 *                          the source was on the battlefield), else every card
 * @param yourTurnsOnly     only on the source's controller's turns ("on each of your turns")
 * @param optional          the player may choose not to reveal ("you may reveal … as you draw it")
 */
public record RevealDrawnCardsEffect(Revealer revealer, boolean firstCardEachTurn, boolean yourTurnsOnly,
                                     boolean optional) implements CardEffect {

    public RevealDrawnCardsEffect {
        Objects.requireNonNull(revealer, "revealer");
    }

    /** "Reveal the first card you draw each turn." */
    public static RevealDrawnCardsEffect firstEachTurn() {
        return new RevealDrawnCardsEffect(Revealer.CONTROLLER, true, false, false);
    }

    /** "Reveal the first card you draw on each of your turns." */
    public static RevealDrawnCardsEffect firstOnEachOfYourTurns() {
        return new RevealDrawnCardsEffect(Revealer.CONTROLLER, true, true, false);
    }

    /** "You may reveal the first card you draw each turn as you draw it." */
    public static RevealDrawnCardsEffect mayRevealFirstEachTurn() {
        return new RevealDrawnCardsEffect(Revealer.CONTROLLER, true, false, true);
    }

    /** "The chosen player reveals each card they draw." */
    public static RevealDrawnCardsEffect eachByChosenPlayer() {
        return new RevealDrawnCardsEffect(Revealer.CHOSEN_PLAYER, false, false, false);
    }

    /** "Players reveal each card they draw." */
    public static RevealDrawnCardsEffect eachByEveryPlayer() {
        return new RevealDrawnCardsEffect(Revealer.EACH_PLAYER, false, false, false);
    }

    /** The optional reveal is asked during the draw, before the next card of a multi-card draw (CR 121.9). */
    @Override
    public boolean pausesDrawInstruction() {
        return optional;
    }

    /** Who reveals the cards they draw. */
    public enum Revealer {
        /** The source's controller. */
        CONTROLLER,
        /** The player the source chose as it entered ({@code ChooseOpponentOnEnterEffect}). */
        CHOSEN_PLAYER,
        /** Every player. */
        EACH_PLAYER
    }
}
