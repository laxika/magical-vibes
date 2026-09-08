package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

@CardRegistration(set = "JOU", collectorNumber = "56")
public class TritonShorestalker extends Card {

    public TritonShorestalker() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
