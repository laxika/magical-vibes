package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDealtAsInfectBelowZeroLifeEffect;

@CardRegistration(set = "NPH", collectorNumber = "18")
public class PhyrexianUnlife extends Card {

    public PhyrexianUnlife() {
        addEffect(EffectSlot.STATIC, new CantLoseGameFromLifeEffect());
        addEffect(EffectSlot.STATIC, new DamageDealtAsInfectBelowZeroLifeEffect());
    }
}
