package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class OrderOfTheAlabasterHost extends Card {

    public OrderOfTheAlabasterHost() {
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, new BoostTargetCreatureEffect(-1, -1));
    }
}
