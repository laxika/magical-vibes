package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "42")
public class WizenedGithzerai extends Card {

    public WizenedGithzerai() {
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER,
                new PerpetuallyBoostCombatOpponentEffect(-2, 0));
    }
}
