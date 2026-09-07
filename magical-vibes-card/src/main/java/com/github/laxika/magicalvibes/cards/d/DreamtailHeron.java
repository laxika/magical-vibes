package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "IKO", collectorNumber = "47")
public class DreamtailHeron extends Card {

    public DreamtailHeron() {
        addEffect(EffectSlot.ON_SELF_MUTATES, new DrawCardEffect());
    }
}
