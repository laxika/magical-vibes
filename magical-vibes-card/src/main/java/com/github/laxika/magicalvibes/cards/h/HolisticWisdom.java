package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithImprintedCardPredicate;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "243")
public class HolisticWisdom extends Card {

    public HolisticWisdom() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new ExileCardFromHandCost(null, null, 1, true),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardSharesCardTypeWithImprintedCardPredicate())
                                .targetGraveyard(true)
                                .build()
                ),
                "{2}, Exile a card from your hand: Return target card from your graveyard to your hand if it shares a card type with the card exiled this way."
        ));
    }
}
