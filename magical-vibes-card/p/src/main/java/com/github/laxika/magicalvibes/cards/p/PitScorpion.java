package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "187")
@CardRegistration(set = "4ED", collectorNumber = "153")
@CardRegistration(set = "LEG", collectorNumber = "114")
public class PitScorpion extends Card {

    public PitScorpion() {
        // Whenever this creature deals damage to a player, that player gets a poison counter.
        addTrigger(Triggers.dealsDamageToPlayer(), new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER));
    }
}
