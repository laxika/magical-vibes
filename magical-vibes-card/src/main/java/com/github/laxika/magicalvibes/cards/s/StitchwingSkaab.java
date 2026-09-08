package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "90")
public class StitchwingSkaab extends Card {

    public StitchwingSkaab() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(
                        new DiscardCardTypeCost(null, null, 2),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .build()
                ),
                "{1}{U}, Discard two cards: Return this card from your graveyard to the battlefield tapped."
        ));
    }
}
