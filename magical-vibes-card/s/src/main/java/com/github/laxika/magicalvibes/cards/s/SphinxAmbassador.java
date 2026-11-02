package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SphinxAmbassadorEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M10", collectorNumber = "73")
public class SphinxAmbassador extends Card {

    public SphinxAmbassador() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new SphinxAmbassadorEffect());
    }
}
