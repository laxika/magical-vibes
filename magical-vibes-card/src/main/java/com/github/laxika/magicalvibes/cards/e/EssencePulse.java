package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "C21", collectorNumber = "39")
public class EssencePulse extends Card {

    public EssencePulse() {
        // You gain 2 life. Each creature gets -X/-X until end of turn, where X is the amount
        // of life you gained this turn.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
        var minusLifeGained = new Scaled(new LifeGainedThisTurn(CountScope.CONTROLLER), -1);
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(minusLifeGained, minusLifeGained));
    }
}
