package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "DGM", collectorNumber = "63")
public class DeadbridgeChant extends Card {

    public DeadbridgeChant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(10, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .returnAtRandom(true)
                .battlefieldIfCreatureElseHand(true)
                .build());
    }
}
