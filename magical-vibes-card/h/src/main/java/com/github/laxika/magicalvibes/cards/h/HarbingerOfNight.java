package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "128")
public class HarbingerOfNight extends Card {

    public HarbingerOfNight() {
        // At the beginning of your upkeep, put a -1/-1 counter on each creature.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), PutCountersEffect.onEach(CounterType.MINUS_ONE_MINUS_ONE, 1, new PermanentIsCreaturePredicate()));
    }
}
