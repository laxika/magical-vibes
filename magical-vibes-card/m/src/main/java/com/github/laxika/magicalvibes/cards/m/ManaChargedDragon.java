package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaThenBoostSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMD", collectorNumber = "129")
@CardRegistration(set = "CMA", collectorNumber = "84")
public class ManaChargedDragon extends Card {

    public ManaChargedDragon() {
        addTrigger(Triggers.attacks(), new EachPlayerPaysAnyManaThenBoostSourceEffect());
        addTrigger(Triggers.blocks(), new EachPlayerPaysAnyManaThenBoostSourceEffect());
    }
}
