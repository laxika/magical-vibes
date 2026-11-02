package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ME1", collectorNumber = "75")
@CardRegistration(set = "ARN", collectorNumber = "30")
public class KhabLGhoul extends Card {

    public KhabLGhoul() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new PutCountersOnSelfEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));
    }
}
