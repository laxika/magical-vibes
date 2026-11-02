package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.CardsLeftGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "55")
@CardRegistration(set = "FIC", collectorNumber = "152")
public class GauFeralYouth extends Card {

    public GauFeralYouth() {
        addTrigger(Triggers.attacks(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                new ConditionalEffect(
                        new CardsLeftGraveyardThisTurn(),
                        new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT)));
    }
}
