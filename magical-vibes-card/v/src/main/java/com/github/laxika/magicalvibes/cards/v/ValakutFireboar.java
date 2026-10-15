package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

@CardRegistration(set = "ROE", collectorNumber = "170")
public class ValakutFireboar extends Card {

    public ValakutFireboar() {
        addEffect(EffectSlot.ON_ATTACK, new SwitchPowerToughnessEffect(true));
    }
}
