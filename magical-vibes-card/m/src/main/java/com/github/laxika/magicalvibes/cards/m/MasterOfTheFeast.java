package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "75")
public class MasterOfTheFeast extends Card {

    public MasterOfTheFeast() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new EachOtherPlayerDrawsCardEffect(1));
    }
}
