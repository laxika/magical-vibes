package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellWithDelayCountersEffect;

@CardRegistration(set = "TMP", collectorNumber = "61")
public class ErtaisMeddling extends Card {

    public ErtaisMeddling() {
        setMinimumXValue(1);
        addSpellEffect(new ExileTargetSpellWithDelayCountersEffect());
    }
}
