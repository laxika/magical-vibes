package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentLifeGainBecomesLifeLossEffect;

@CardRegistration(set = "ORI", collectorNumber = "120")
public class TaintedRemedy extends Card {

    public TaintedRemedy() {
        addEffect(EffectSlot.STATIC, new OpponentLifeGainBecomesLifeLossEffect());
    }
}
