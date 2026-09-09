package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PayXManaMoveCountersFromSourceToTargetCreatureEffect;

@CardRegistration(set = "SOS", collectorNumber = "69")
public class TesterOfTheTangential extends Card {

    public TesterOfTheTangential() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new PayXManaMoveCountersFromSourceToTargetCreatureEffect());
    }
}
