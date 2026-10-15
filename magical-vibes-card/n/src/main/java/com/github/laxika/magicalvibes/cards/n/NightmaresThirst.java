package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M19", collectorNumber = "111")
public class NightmaresThirst extends Card {

    public NightmaresThirst() {
        // You gain 1 life. Target creature gets -X/-X until end of turn, where X is the amount
        // of life you gained this turn. (The 1 life from this spell is included in X.)
        var minusLifeGained = new Scaled(new LifeGainedThisTurn(CountScope.CONTROLLER), -1);
        addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(minusLifeGained, minusLifeGained));
    }
}
