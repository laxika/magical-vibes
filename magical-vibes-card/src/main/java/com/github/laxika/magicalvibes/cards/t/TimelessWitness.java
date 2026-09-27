package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "MH2", collectorNumber = "179")
public class TimelessWitness extends Card {

    public TimelessWitness() {
        // When this creature enters, return target card from your graveyard to your hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .targetGraveyard(true)
                .build());

        // Eternalize {5}{G}{G}.
        addEternalize("{5}{G}{G}", "Human Shaman");
    }
}
