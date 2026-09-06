package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenDealDamageToEnteringCreatureEffect;

@CardRegistration(set = "DIS", collectorNumber = "61")
public class FlameKinWarScout extends Card {

    public FlameKinWarScout() {
        // When another creature enters, sacrifice this creature. If you do, it deals 4 damage to that creature.
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new SacrificeSelfThenDealDamageToEnteringCreatureEffect(4));
    }
}
