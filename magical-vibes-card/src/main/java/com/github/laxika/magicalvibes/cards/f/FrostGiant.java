package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;

@CardRegistration(set = "LEG", collectorNumber = "148")
public class FrostGiant extends Card {

    public FrostGiant() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new RampageEffect(2));
    }
}
