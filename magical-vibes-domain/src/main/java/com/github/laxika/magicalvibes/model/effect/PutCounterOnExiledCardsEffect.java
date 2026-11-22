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
 */
public record PutCounterOnExiledCardsEffect(CardSubject cards, ExiledCardCounter counter) implements CardEffect {

    public PutCounterOnExiledCardsEffect {
        Objects.requireNonNull(cards, "cards");
        Objects.requireNonNull(counter, "counter");
        if (cards.declaresTarget()) {
            throw new IllegalArgumentException("The step names the cards an earlier step moved: " + cards);
        }
    }

    /** "Put a [counter] counter on it" of the cards the exile step before it exiled. */
    public static PutCounterOnExiledCardsEffect onCardsExiledThisWay(ExiledCardCounter counter) {
        return new PutCounterOnExiledCardsEffect(Subjects.cardsThisWay(StepKind.EXILE), counter);
    }
}
