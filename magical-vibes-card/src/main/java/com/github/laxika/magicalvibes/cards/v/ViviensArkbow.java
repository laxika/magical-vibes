package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "181")
public class ViviensArkbow extends Card {

    public ViviensArkbow() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new LookAtTopCardsEffect(
                                new XValue(), new Fixed(1), new CardTypePredicate(CardType.CREATURE),
                                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                                LibrarySearchDestination.BATTLEFIELD, true, false, new XValue())
                ),
                "{X}, {T}, Discard a card: Look at the top X cards of your library. You may put a creature "
                        + "card with mana value X or less from among them onto the battlefield. Put the rest "
                        + "on the bottom of your library in a random order."
        ));
    }
}
