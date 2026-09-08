package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "49")
public class AdvancedStitchwing extends Card {

    public AdvancedStitchwing() {
        // {2}{U}, Discard two cards: Return this card from your graveyard to the battlefield tapped.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(
                        new DiscardCardTypeCost(null, null, 2),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .build()
                ),
                "{2}{U}, Discard two cards: Return this card from your graveyard to the battlefield tapped."
        ));
    }
}
