package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;

@CardRegistration(set = "5DN", collectorNumber = "145")
public class RazorgrassScreen extends Card {

    public RazorgrassScreen() {
        addEffect(EffectSlot.STATIC, new MustBlockEachCombatEffect());
    }
}
