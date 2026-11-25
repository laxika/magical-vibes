package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.KinshipEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "139")
public class WinnowerPatrol extends Card {

    public WinnowerPatrol() {
        // Kinship — At the beginning of your upkeep, you may look at the top card of your library.
        // If it shares a creature type with this creature, you may reveal it. If you do, put a
        // +1/+1 counter on this creature.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new KinshipEffect(List.of(
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE))));
    }
}
