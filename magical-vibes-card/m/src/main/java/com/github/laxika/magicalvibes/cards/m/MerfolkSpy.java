package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "66")
@CardRegistration(set = "M14", collectorNumber = "62")
public class MerfolkSpy extends Card {

    public MerfolkSpy() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RevealRandomCardFromTargetPlayerHandEffect());
    }
}
