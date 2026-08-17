package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "154")
public class BeastriderVanguard extends Card {

    public BeastriderVanguard() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}",
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(
                        3, new CardIsPermanentPredicate())),
                "{4}{G}: Look at the top three cards of your library. You may reveal a permanent card from among them and put it into your hand. Put the rest on the bottom of your library in any order."
        ));
    }
}
