package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OppositionAgentEffect;

@CardRegistration(set = "MAR", collectorNumber = "19")
@CardRegistration(set = "OMB", collectorNumber = "19")
public class OppositionAgent extends Card {

    public OppositionAgent() {
        addEffect(EffectSlot.STATIC, new OppositionAgentEffect());
    }
}
