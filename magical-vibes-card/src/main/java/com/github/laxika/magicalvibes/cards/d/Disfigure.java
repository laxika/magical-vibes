package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "ZEN", collectorNumber = "87")
@CardRegistration(set = "M20", collectorNumber = "95")
@CardRegistration(set = "BRO", collectorNumber = "91")
@CardRegistration(set = "DDR", collectorNumber = "46")
public class Disfigure extends Card {

    public Disfigure() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-2, -2));
    }
}
