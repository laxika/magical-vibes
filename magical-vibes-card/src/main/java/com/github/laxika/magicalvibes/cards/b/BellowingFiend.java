package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageDamagedCreatureControllerAndSelfEffect;

@CardRegistration(set = "7ED", collectorNumber = "119")
public class BellowingFiend extends Card {

    public BellowingFiend() {
        // Whenever this creature deals damage to a creature, this creature deals 3 damage to that
        // creature's controller and 3 damage to you.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                new DamageDamagedCreatureControllerAndSelfEffect(3, 3));
    }
}
