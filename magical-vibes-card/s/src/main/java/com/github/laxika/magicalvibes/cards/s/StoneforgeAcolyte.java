package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "38")
public class StoneforgeAcolyte extends Card {

    public StoneforgeAcolyte() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapCreatureCost(new PermanentHasSubtypePredicate(CardSubtype.ALLY), true, false),
                        LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(
                                4, new CardSubtypePredicate(CardSubtype.EQUIPMENT))
                ),
                "{T}, Tap an untapped Ally you control: Look at the top four cards of your library. "
                        + "You may reveal an Equipment card from among them and put it into your hand. "
                        + "Put the rest on the bottom of your library in any order."
        ));
    }
}
