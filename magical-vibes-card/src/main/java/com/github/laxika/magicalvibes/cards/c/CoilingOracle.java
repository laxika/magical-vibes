package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardLandToBattlefieldElseToHandEffect;

@CardRegistration(set = "DIS", collectorNumber = "107")
@CardRegistration(set = "DDO", collectorNumber = "51")
@CardRegistration(set = "MM3", collectorNumber = "157")
@CardRegistration(set = "GK2", collectorNumber = "115")
@CardRegistration(set = "2X2", collectorNumber = "194")
@CardRegistration(set = "RVR", collectorNumber = "172")
public class CoilingOracle extends Card {

    public CoilingOracle() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RevealTopCardLandToBattlefieldElseToHandEffect());
    }
}
