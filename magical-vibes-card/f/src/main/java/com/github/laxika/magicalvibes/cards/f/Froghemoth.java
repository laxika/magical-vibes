package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileUpToCombatDamageCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "184")
public class Froghemoth extends Card {

    public Froghemoth() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileUpToCombatDamageCardsFromGraveyardEffect());
    }
}
