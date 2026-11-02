package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "404")
@CardRegistration(set = "ICE", collectorNumber = "342")
@CardRegistration(set = "ME2", collectorNumber = "223")
public class TimeBomb extends Card {

    public TimeBomb() {
        // At the beginning of your upkeep, put a time counter on Time Bomb.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new PutCountersOnSelfEffect(CounterType.TIME));

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
