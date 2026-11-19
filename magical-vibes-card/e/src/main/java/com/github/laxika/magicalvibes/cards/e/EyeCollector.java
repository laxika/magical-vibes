package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "86")
public class EyeCollector extends Card {

    public EyeCollector() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MillEffect(1, Subjects.eachPlayer()));
    }
}
