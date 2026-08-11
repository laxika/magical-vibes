package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "M15", collectorNumber = "26")
@CardRegistration(set = "MRD", collectorNumber = "16")
@CardRegistration(set = "M20", collectorNumber = "34")
public class RaiseTheAlarm extends Card {

    public RaiseTheAlarm() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.whiteSoldier(2));
    }
}
