package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

@CardRegistration(set = "ULG", collectorNumber = "73")
public class AboutFace extends Card {

    public AboutFace() {
        addEffect(EffectSlot.SPELL, new SwitchPowerToughnessEffect());
    }
}
