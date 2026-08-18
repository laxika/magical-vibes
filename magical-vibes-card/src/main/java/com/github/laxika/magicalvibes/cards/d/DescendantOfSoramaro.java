package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "33")
public class DescendantOfSoramaro extends Card {

    public DescendantOfSoramaro() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new ReorderTopCardsOfLibraryEffect(new CardsInHand(CountScope.CONTROLLER))),
                "{1}{U}: Look at the top X cards of your library, where X is the number of cards in your hand, then put them back in any order."
        ));
    }
}
