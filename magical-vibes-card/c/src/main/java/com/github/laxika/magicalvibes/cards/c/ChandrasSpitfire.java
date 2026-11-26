package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "129")
@CardRegistration(set = "M20", collectorNumber = "132")
public class ChandrasSpitfire extends Card {

    public ChandrasSpitfire() {
        addTrigger(Triggers.opponentDealtNoncombatDamage(), BoostEffect.self(3, 0));
    }
}
