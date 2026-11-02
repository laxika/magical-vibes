package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STH", collectorNumber = "88")
public class HeatOfBattle extends Card {

    public HeatOfBattle() {
        addTrigger(Triggers.creatureBlocks(),
                new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
