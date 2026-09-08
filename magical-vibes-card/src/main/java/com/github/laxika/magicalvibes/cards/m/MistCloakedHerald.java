package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

@CardRegistration(set = "RIX", collectorNumber = "43")
public class MistCloakedHerald extends Card {

    public MistCloakedHerald() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
