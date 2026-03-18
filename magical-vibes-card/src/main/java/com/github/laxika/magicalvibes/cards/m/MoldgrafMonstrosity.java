package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ISD", collectorNumber = "194")
public class MoldgrafMonstrosity extends Card {

    public MoldgrafMonstrosity() {
        // When Moldgraf Monstrosity dies, exile it, then return two creature cards
        // at random from your graveyard to the battlefield.
        addEffect(EffectSlot.ON_DEATH, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .returnAtRandom(true)
                .randomCount(2)
                .exileSourceFromGraveyard(true)
                .build());
    }
}
