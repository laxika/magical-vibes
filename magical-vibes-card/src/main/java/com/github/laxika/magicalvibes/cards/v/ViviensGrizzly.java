package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "182")
public class ViviensGrizzly extends Card {

    public ViviensGrizzly() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new LookAtTopCardMayRevealMatchingToHandEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.PLANESWALKER))),
                        LookAtTopCardMayRevealMatchingToHandEffect.OtherwiseDestination.BOTTOM)),
                "{3}: Look at the top card of your library. If it's a creature or planeswalker card, "
                        + "you may reveal it and put it into your hand. If you don't put the card into "
                        + "your hand, put it on the bottom of your library."
        ));
    }
}
