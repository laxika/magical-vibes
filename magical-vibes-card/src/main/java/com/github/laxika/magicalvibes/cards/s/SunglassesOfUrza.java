package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SpendWhiteManaAsRedEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "4ED", collectorNumber = "347")
public class SunglassesOfUrza extends Card {

    public SunglassesOfUrza() {
        // "You may spend white mana as though it were red mana."
        addEffect(EffectSlot.STATIC, new SpendWhiteManaAsRedEffect());
    }
}
