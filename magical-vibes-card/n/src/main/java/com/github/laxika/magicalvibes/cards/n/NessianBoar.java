package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "181")
@CardRegistration(set = "THB", collectorNumber = "328")
public class NessianBoar extends Card {

    public NessianBoar() {
        addStaticEffect(new MustBeBlockedByAllCreaturesEffect());
        addTrigger(Triggers.becomesBlocked(),
                TriggerOptions.EACH_BLOCKER,
                new TargetPermanentControllerDrawsCardEffect());
    }
}
