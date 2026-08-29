package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "FDN", collectorNumber = "112")
public class SpinnerOfSouls extends Card {

    public SpinnerOfSouls() {
        // Whenever another nontoken creature you control dies, you may reveal cards from the top
        // of your library until you reveal a creature card, putting it into your hand and the rest
        // on the bottom of your library in a random order.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new MayEffect(
                        new RevealUntilCardPredicateRestOnBottomRandomEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.HAND),
                        "Reveal cards from the top of your library until you reveal a creature card?"));
    }
}
