package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;

import java.util.Objects;

/**
 * Seeks matching cards into hand and makes the delayed triggered ability that discards those exact cards at the
 * beginning of an end step (CR 603.7a): "at the beginning of your next end step" (the default), "at the beginning of
 * the next end step" ({@link #nextEndStep}) or "at the beginning of your next turn's end step"
 * ({@link #nextTurnsEndStep}), which is the end step of a later turn of the controller's.
 *
 * @param count     how many cards are sought
 * @param filter    the cards that may be sought
 * @param endStepOf whose end step it waits for: {@link ActivePlayer#YOU} or {@link ActivePlayer#ANY}
 * @param laterTurn whether it waits past the turn it was made in
 */
public record SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect(
        DynamicAmount count, CardPredicate filter, ActivePlayer endStepOf, boolean laterTurn)
        implements CardEffect {

    public SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect {
        Objects.requireNonNull(count, "count");
        Objects.requireNonNull(endStepOf, "endStepOf");
    }

    /** Seeks {@code count} cards, discarded at the beginning of the controller's next end step. */
    public SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect(DynamicAmount count, CardPredicate filter) {
        this(count, filter, ActivePlayer.YOU, false);
    }

    /** Seeks one card, discarded at the beginning of the controller's next end step. */
    public SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect(CardPredicate filter) {
        this(new Fixed(1), filter);
    }

    /** Seeks one card, discarded at the beginning of the next end step, whoever's it is (Arius, Flyby Trawler). */
    public static SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect nextEndStep(CardPredicate filter) {
        return new SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect(
                new Fixed(1), filter, ActivePlayer.ANY, false);
    }

    /** Seeks one card, discarded at the beginning of the controller's next turn's end step (Perilous Iteration). */
    public static SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect nextTurnsEndStep(CardPredicate filter) {
        return new SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect(
                new Fixed(1), filter, ActivePlayer.YOU, true);
    }
}
