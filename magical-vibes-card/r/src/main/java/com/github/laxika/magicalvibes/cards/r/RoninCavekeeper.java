package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "111")
public class RoninCavekeeper extends Card {

    public RoninCavekeeper() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new BushidoEffect(2));
        addTrigger(Triggers.becomesBlocked(), new BushidoEffect(2));
    }
}
