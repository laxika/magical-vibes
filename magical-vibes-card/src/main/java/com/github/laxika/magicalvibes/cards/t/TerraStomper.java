package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;

@CardRegistration(set = "ZEN", collectorNumber = "187")
public class TerraStomper extends Card {

    public TerraStomper() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
    }
}
