package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.NykthosParagonLifeGainEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "22")
public class NykthosParagon extends Card {

    public NykthosParagon() {
        // Whenever you gain life, you may put that many +1/+1 counters on each creature you control.
        // Do this only once each turn.
        addTrigger(Triggers.youGainLife(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new NykthosParagonLifeGainEffect());
    }
}
