package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;

import java.util.Objects;

/**
 * "... with a [name] counter on it" / "and put a [name] counter on each of them" after an exile step: each card
 * {@code cards} names that is in exile gets {@code count} {@code counter} counters, in the one store for counters
 * on exiled cards ({@code GameData.exiledCardCounters}). A card that has left exile since is a new object
 * (CR 400.7) and gets none; a step that exiled nothing names nothing (Altaïr Ibn-La'Ahad, The Animus, Rex,
 * Cyber-Hound, Grolnok, the Omnivore, Pako, Arcane Retriever, Karn, Scion of Urza). A fetch counter also makes the
 * effect's controller the card's exiler (Pako's "cards exiled with fetch counters by you").
 *
 * @param cards   which cards; never a target (the exile step before it declares any)
 * @param counter the kind of counter
 * @param count   how many each card gets
 */
public record PutCounterOnExiledCardsEffect(CardSubject cards, CounterType counter, int count)
        implements CardEffect {

    public PutCounterOnExiledCardsEffect {
        Objects.requireNonNull(cards, "cards");
        Objects.requireNonNull(counter, "counter");
        if (cards.declaresTarget()) {
            throw new IllegalArgumentException("The step names the cards an earlier step moved: " + cards);
        }
        if (count < 1) {
            throw new IllegalArgumentException("At least one counter: " + count);
        }
    }

    /** One counter on each card. */
    public PutCounterOnExiledCardsEffect(CardSubject cards, CounterType counter) {
        this(cards, counter, 1);
    }

    /** "Put [count] refine counters on it" of the cards the exile step before it exiled. */
    public static PutCounterOnExiledCardsEffect refineOnCardsExiledThisWay(int count) {
        return new PutCounterOnExiledCardsEffect(
                Subjects.cardsThisWay(StepKind.EXILE), CounterType.REFINE, count);
    }

    /** "Put a [counter] counter on it" of the cards the exile step before it exiled. */
    public static PutCounterOnExiledCardsEffect onCardsExiledThisWay(CounterType counter) {
        return new PutCounterOnExiledCardsEffect(Subjects.cardsThisWay(StepKind.EXILE), counter);
    }
}
