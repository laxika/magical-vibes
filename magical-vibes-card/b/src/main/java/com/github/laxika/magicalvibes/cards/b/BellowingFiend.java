package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageDamagedCreatureControllerAndSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "7ED", collectorNumber = "119")
@CardRegistration(set = "TMP", collectorNumber = "108")
public class BellowingFiend extends Card {

    public BellowingFiend() {
        // Whenever this creature deals damage to a creature, this creature deals 3 damage to that
        // creature's controller and 3 damage to you.
        addTrigger(Triggers.creatureYouControlDealsDamageToCreature(),
                new DamageDamagedCreatureControllerAndSelfEffect(3, 3));
    }
}
