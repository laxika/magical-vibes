package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "SNC", collectorNumber = "98")
public class VampireScrivener extends Card {

    public VampireScrivener() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new ConditionalEffect(new ControllerTurn(), new PutCountersOnSourceEffect(1, 1, 1)));
        addEffect(EffectSlot.ON_CONTROLLER_LOSES_LIFE,
                new ConditionalEffect(new ControllerTurn(), new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
