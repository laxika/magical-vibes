package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "105")
public class MistyMountainsRaider extends Card {

    public MistyMountainsRaider() {
        addTrigger(Triggers.youAttack(), new AmassGoblinsEffect(2));
    }
}
