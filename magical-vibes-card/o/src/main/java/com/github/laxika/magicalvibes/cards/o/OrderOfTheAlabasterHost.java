package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

public class OrderOfTheAlabasterHost extends Card {

    public OrderOfTheAlabasterHost() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new BoostTargetCreatureEffect(-1, -1), TriggerMode.PER_BLOCKER);
    }
}
