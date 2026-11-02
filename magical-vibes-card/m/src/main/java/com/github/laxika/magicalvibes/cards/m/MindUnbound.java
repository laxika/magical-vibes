package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenDrawPerCounterEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "68")
public class MindUnbound extends Card {

    public MindUnbound() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new PutCounterOnSelfThenDrawPerCounterEffect(CounterType.LORE));
    }
}
