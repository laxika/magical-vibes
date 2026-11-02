package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "23")
public class HeraldsOfTzeentch extends Card {

    public HeraldsOfTzeentch() {
        addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());
    }
}
