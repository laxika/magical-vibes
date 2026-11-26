package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "109")
public class EscapedNull extends Card {

    public EscapedNull() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, BoostEffect.self(5, 0));
        addTrigger(Triggers.becomesBlocked(), BoostEffect.self(5, 0));
    }
}
