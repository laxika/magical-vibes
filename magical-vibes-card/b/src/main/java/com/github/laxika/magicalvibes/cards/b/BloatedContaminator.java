package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "159")
public class BloatedContaminator extends Card {

    public BloatedContaminator() {
        addStaticEffect(new ToxicEffect(1));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ProliferateEffect());
    }
}
