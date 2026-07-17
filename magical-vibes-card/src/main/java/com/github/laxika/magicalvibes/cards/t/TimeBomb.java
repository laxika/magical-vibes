package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "404")
public class TimeBomb extends Card {

    public TimeBomb() {
        // At the beginning of your upkeep, put a time counter on Time Bomb.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.TIME));

        // {1}, {T}, Sacrifice Time Bomb: It deals damage equal to the number of time
        // counters on it to each creature and each player.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new MassDamageEffect(new CountersOnSource(CounterType.TIME), true)
                ),
                "{1}, {T}, Sacrifice Time Bomb: It deals damage equal to the number of time counters on it to each creature and each player."
        ));
    }
}
