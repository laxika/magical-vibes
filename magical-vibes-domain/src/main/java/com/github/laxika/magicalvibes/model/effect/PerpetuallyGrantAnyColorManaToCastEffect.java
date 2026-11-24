package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "It perpetually gains prismatic", "that card perpetually gains "You may spend mana as though it were mana of any
 * color to cast this spell."": the cards {@code cards} names may be cast with mana spent as though it were mana of any
 * color, for as long as they are those cards (digital only). It doesn't let mana of a color pay a colorless cost
 * ({@code {C}}); "mana of any type can be spent to cast this spell" is another permission. Usually after a conjure, over
 * {@code Subjects.cardsThisWay(StepKind.CONJURE)}.
 *
 * @param cards the cards that gain it; a hand's or library's are named in no log (CR 400.2)
 */
public record PerpetuallyGrantAnyColorManaToCastEffect(CardSubject cards) implements CardEffect {

    public PerpetuallyGrantAnyColorManaToCastEffect {
        Objects.requireNonNull(cards, "cards");
    }

    @Override
    public TargetSpec targetSpec() {
        return cards.targetSpec();
    }
}
