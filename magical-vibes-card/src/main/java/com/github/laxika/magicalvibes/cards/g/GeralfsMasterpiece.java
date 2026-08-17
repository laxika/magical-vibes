package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "65")
public class GeralfsMasterpiece extends Card {

    public GeralfsMasterpiece() {
        Scaled minusOnePerCardInHand = new Scaled(new CardsInHand(CountScope.CONTROLLER), -1);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(minusOnePerCardInHand, minusOnePerCardInHand));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(
                        new DiscardCardTypeCost(null, null, 3),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .build()),
                "{3}{U}, Discard three cards: Return this card from your graveyard to the battlefield tapped."
        ));
    }
}
