package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;

@CardRegistration(set = "TDM", collectorNumber = "89")
public class SandskitterOutrider extends Card {

    public SandskitterOutrider() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EndureEffect(2));
    }
}
