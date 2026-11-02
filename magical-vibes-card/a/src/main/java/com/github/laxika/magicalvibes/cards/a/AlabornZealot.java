package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "P02", collectorNumber = "6")
public class AlabornZealot extends Card {

    public AlabornZealot() {
        addTrigger(Triggers.blocks(), new DestroyBlockedCreatureAndSelfEffect());
    }
}
