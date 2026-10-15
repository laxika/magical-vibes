package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndSaddleEffect;

@CardRegistration(set = "NEO", collectorNumber = "16")
public class HotshotMechanic extends Card {

    public HotshotMechanic() {
        addEffect(EffectSlot.STATIC, new PowerBoostForCrewAndSaddleEffect(2));
    }
}
