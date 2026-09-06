package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardLandToBattlefieldElseToHandEffect;

@CardRegistration(set = "DIS", collectorNumber = "107")
public class CoilingOracle extends Card {

    public CoilingOracle() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RevealTopCardLandToBattlefieldElseToHandEffect());
    }
}
