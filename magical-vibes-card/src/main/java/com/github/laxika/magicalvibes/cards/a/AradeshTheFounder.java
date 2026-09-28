package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AradeshTheFounderEffect;

@CardRegistration(set = "PIP", collectorNumber = "9")
@CardRegistration(set = "PIP", collectorNumber = "362")
@CardRegistration(set = "PIP", collectorNumber = "890")
@CardRegistration(set = "PIP", collectorNumber = "537")
public class AradeshTheFounder extends Card {

    public AradeshTheFounder() {
        addEffect(EffectSlot.STATIC, new AradeshTheFounderEffect());
    }
}
