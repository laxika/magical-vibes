package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.VoidCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "140")
public class KavaronSkywarden extends Card {

    public KavaronSkywarden() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(new VoidCondition(), new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
