package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "59")
public class Browse extends Card {

    public Browse() {
        // {2}{U}{U}: Look at the top five cards of your library, put one of them into your hand,
        // and exile the rest.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{U}{U}",
                List.of(LookAtTopCardsEffect.chooseOneToHandRestToExile(new Fixed(5))),
                "{2}{U}{U}: Look at the top five cards of your library, put one of them into your hand, and exile the rest."
        ));
    }
}
