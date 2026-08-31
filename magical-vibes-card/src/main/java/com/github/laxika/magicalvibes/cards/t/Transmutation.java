package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

@CardRegistration(set = "CHR", collectorNumber = "40")
@CardRegistration(set = "LEG", collectorNumber = "123")
public class Transmutation extends Card {

    public Transmutation() {
        addEffect(EffectSlot.SPELL, new SwitchPowerToughnessEffect());
    }
}
