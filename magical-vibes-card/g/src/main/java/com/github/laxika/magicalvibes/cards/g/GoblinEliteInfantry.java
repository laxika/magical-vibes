package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "206")
@CardRegistration(set = "7ED", collectorNumber = "187")
@CardRegistration(set = "6ED", collectorNumber = "183")
@CardRegistration(set = "MIR", collectorNumber = "177")
public class GoblinEliteInfantry extends Card {

    public GoblinEliteInfantry() {
        addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK, new BoostSelfEffect(-1, -1));
        addTrigger(Triggers.becomesBlocked(), new BoostSelfEffect(-1, -1));
    }
}
