package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "HBG", collectorNumber = "164")
public class NefariousImp extends Card {

    public NefariousImp() {
        addEffect(EffectSlot.ON_ALLY_PERMANENTS_LEAVE_BATTLEFIELD, new ScryEffect(1));
    }
}
