package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "INR", collectorNumber = "28")
public class InspiringCaptain extends Card {

    public InspiringCaptain() {
        // When this creature enters, creatures you control get +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostAllOwnCreaturesEffect(1, 1));
    }
}
