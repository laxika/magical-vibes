package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CommanderCastsFromCommandZoneThisGame;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCommanderCastCostEffect;

@CardRegistration(set = "C18", collectorNumber = "32")
public class MythUnbound extends Card {

    public MythUnbound() {
        addEffect(EffectSlot.STATIC,
                new ReduceCommanderCastCostEffect(new CommanderCastsFromCommandZoneThisGame()));
        addEffect(EffectSlot.ON_YOUR_COMMANDER_PUT_INTO_COMMAND_ZONE, new DrawCardEffect(1));
    }
}
