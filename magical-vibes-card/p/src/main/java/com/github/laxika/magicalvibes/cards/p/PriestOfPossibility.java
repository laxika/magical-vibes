package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopSevenAndPerpetuallyGainKeywordsEffect;

@CardRegistration(set = "YDMU", collectorNumber = "2")
public class PriestOfPossibility extends Card {

    public PriestOfPossibility() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LookAtTopSevenAndPerpetuallyGainKeywordsEffect());
    }
}
