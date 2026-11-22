package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ExiledCardCounter;
import com.github.laxika.magicalvibes.model.StepKind;

import java.util.Objects;

/**
 * "... with a [name] counter on it" / "and put a [name] counter on each of them" after an exile step: each card
 * {@code cards} names that is in exile gets one {@code counter}. A card that has left exile since is a new object
 * (CR 400.7) and gets none; a step that exiled nothing names nothing (Altaïr Ibn-La'Ahad, The Animus, Rex,
 * Cyber-Hound, Grolnok, the Omnivore, Pako, Arcane Retriever).
 *
 * @param cards   which cards; never a target (the exile step before it declares any)
 * @param counter the kind of counter
 * @param count   how many each card gets; more than one only of a counter whose store counts them
 */
public record PutCounterOnExiledCardsEffect(CardSubject cards, ExiledCardCounter counter, int count)
        implements CardEffect {

    public PutCounterOnExiledCardsEffect {
        Objects.requireNonNull(cards, "cards");
        Objects.requireNonNull(counter, "counter");
        if (cards.declaresTarget()) {
            throw new IllegalArgumentException("The step names the cards an earlier step moved: " + cards);
        }
        if (count < 1 || count > 1 && counter != ExiledCardCounter.REFINE) {
            throw new IllegalArgumentException("Only refine counters are put several at a time: " + count);
        }
    }

    /** One counter on each card. */
    public PutCounterOnExiledCardsEffect(CardSubject cards, ExiledCardCounter counter) {
        this(cards, counter, 1);
    }

    /** "Put [count] refine counters on it" of the cards the exile step before it exiled. */
    public static PutCounterOnExiledCardsEffect refineOnCardsExiledThisWay(int count) {
        return new PutCounterOnExiledCardsEffect(
                Subjects.cardsThisWay(StepKind.EXILE), ExiledCardCounter.REFINE, count);
    }

    /** "Put a [counter] counter on it" of the cards the exile step before it exiled. */
    public static PutCounterOnExiledCardsEffect onCardsExiledThisWay(ExiledCardCounter counter) {
        return new PutCounterOnExiledCardsEffect(Subjects.cardsThisWay(StepKind.EXILE), counter);
    }
}
