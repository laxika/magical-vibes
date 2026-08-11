package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "166")
public class BrightwoodTracker extends Card {

    public BrightwoodTracker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}{G}",
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(4, new CardTypePredicate(CardType.CREATURE))),
                "{5}{G}, {T}: Look at the top four cards of your library. You may reveal a creature card from among them and put it into your hand. Put the rest on the bottom of your library in a random order."
        ));
    }
}
