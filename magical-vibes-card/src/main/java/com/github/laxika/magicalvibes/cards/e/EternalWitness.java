package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "5DN", collectorNumber = "86")
@CardRegistration(set = "DDJ", collectorNumber = "55")
@CardRegistration(set = "MMA", collectorNumber = "144")
@CardRegistration(set = "MB1", collectorNumber = "163")
@CardRegistration(set = "UMA", collectorNumber = "163")
@CardRegistration(set = "2X2", collectorNumber = "145")
@CardRegistration(set = "TSR", collectorNumber = "361")
@CardRegistration(set = "AA2", collectorNumber = "13")
@CardRegistration(set = "CMD", collectorNumber = "152")
@CardRegistration(set = "C15", collectorNumber = "183")
public class EternalWitness extends Card {

    public EternalWitness() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .targetGraveyard(true)
                .build(), "Return the targeted card from your graveyard to your hand?"));
    }
}
