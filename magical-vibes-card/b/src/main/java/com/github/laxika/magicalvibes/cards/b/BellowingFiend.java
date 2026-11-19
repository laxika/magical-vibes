package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "7ED", collectorNumber = "119")
@CardRegistration(set = "TMP", collectorNumber = "108")
public class BellowingFiend extends Card {

    public BellowingFiend() {
        // Whenever this creature deals damage to a creature, this creature deals 3 damage to that
        // creature's controller and 3 damage to you.
        addTrigger(Triggers.dealsDamageToCreature(),
                new DealDamageToPlayersEffect(3, Subjects.controllerOf(Subjects.triggering())),
                new DealDamageToPlayersEffect(3, Subjects.you()));
    }
}
