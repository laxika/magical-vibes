package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "232")
public class InscribedTablet extends Card {

    public InscribedTablet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new LookAtTopCardsEffect(
                                new Fixed(5), new Fixed(1), new CardTypePredicate(CardType.LAND),
                                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, true,
                                LibrarySearchDestination.HAND, false, false, null,
                                new DrawCardEffect(1))
                ),
                "{1}, {T}, Sacrifice this artifact: Reveal the top five cards of your library. "
                        + "Put a land card from among them into your hand and the rest on the bottom "
                        + "of your library in a random order. If you didn't put a card into your hand "
                        + "this way, draw a card."
        ));
    }
}
