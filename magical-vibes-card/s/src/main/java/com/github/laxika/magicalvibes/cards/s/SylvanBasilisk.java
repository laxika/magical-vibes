package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "301")
@CardRegistration(set = "P02", collectorNumber = "146")
@CardRegistration(set = "S99", collectorNumber = "145")
public class SylvanBasilisk extends Card {

    public SylvanBasilisk() {
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER, new DestroyCreatureBlockingThisEffect());
    }
}
