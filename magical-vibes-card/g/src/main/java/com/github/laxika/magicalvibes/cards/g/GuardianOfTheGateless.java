package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockedBySource;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "14")
public class GuardianOfTheGateless extends Card {

    public GuardianOfTheGateless() {
        addStaticEffect(new CanBlockAnyNumberOfCreaturesEffect());
        addTrigger(Triggers.blocks(),
                TriggerOptions.ONE_OR_MORE,
                new BoostSelfEffect(new CreaturesBlockedBySource(), new CreaturesBlockedBySource()));
    }
}
