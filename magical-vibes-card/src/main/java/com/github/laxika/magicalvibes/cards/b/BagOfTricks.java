package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.RollD8Effect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "37")
public class BagOfTricks extends Card {

    public BagOfTricks() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{G}",
                List.of(new RollD8Effect(new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardManaValueEqualsXPredicate())),
                        LibrarySearchDestination.BATTLEFIELD))),
                "{4}{G}, {T}: Roll a d8. Reveal cards from the top of your library until you reveal a creature card with mana value equal to the result. Put that card onto the battlefield and the rest on the bottom of your library in a random order."
        ));
    }
}
