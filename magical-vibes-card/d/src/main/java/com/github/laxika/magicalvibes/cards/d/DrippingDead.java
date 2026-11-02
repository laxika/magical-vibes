package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "67")
public class DrippingDead extends Card {

    public DrippingDead() {
        addStaticEffect(new CantBlockEffect());
        addTrigger(Triggers.creatureYouControlDealsDamageToCreature(),
                new DestroyDamagedCreatureEffect(true, true));
    }
}
