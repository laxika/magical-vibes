package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "22")
public class ShuEliteInfantry extends Card {

    public ShuEliteInfantry() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockAloneEffect());
    }
}
