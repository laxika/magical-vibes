package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "282")
public class VenomousDragonfly extends Card {

    public VenomousDragonfly() {
        // Whenever this creature blocks or becomes blocked by a creature,
        // destroy that creature at end of combat.
        DestroyCombatOpponentAtEndOfCombatEffect destroyAtEndOfCombat =
                new DestroyCombatOpponentAtEndOfCombatEffect(false);
        addTrigger(Triggers.blocks(), destroyAtEndOfCombat);
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, destroyAtEndOfCombat);
    }
}
