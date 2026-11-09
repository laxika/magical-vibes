package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "94")
public class TangleAsp extends Card {

    public TangleAsp() {
        DestroyCombatOpponentAtEndOfCombatEffect destroyAtEndOfCombat =
                new DestroyCombatOpponentAtEndOfCombatEffect(false);
        addTrigger(Triggers.blocks(), destroyAtEndOfCombat);
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, destroyAtEndOfCombat);
    }
}
