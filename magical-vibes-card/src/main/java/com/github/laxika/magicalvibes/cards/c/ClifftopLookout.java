package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandToBattlefieldRestToBottomEffect;

@CardRegistration(set = "BLB", collectorNumber = "168")
public class ClifftopLookout extends Card {

    public ClifftopLookout() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RevealUntilLandToBattlefieldRestToBottomEffect(true));
    }
}
