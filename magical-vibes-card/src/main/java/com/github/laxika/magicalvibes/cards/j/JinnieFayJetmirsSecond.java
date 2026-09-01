package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.JinnieFayTokenReplacementEffect;

@CardRegistration(set = "SNC", collectorNumber = "195")
public class JinnieFayJetmirsSecond extends Card {

    public JinnieFayJetmirsSecond() {
        addEffect(EffectSlot.STATIC, new JinnieFayTokenReplacementEffect());
    }
}
