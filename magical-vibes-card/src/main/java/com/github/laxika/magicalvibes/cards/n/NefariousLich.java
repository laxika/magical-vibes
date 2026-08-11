package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameOnLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.NefariousLichDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.NefariousLichLifeGainReplacementEffect;

@CardRegistration(set = "ODY", collectorNumber = "153")
public class NefariousLich extends Card {

    public NefariousLich() {
        addEffect(EffectSlot.STATIC, new NefariousLichDamageReplacementEffect());
        addEffect(EffectSlot.STATIC, new NefariousLichLifeGainReplacementEffect());
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new ControllerLosesGameOnLeavesEffect());
    }
}
