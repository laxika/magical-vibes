package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.CardsLeftGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "FIC", collectorNumber = "55")
@CardRegistration(set = "FIC", collectorNumber = "152")
public class GauFeralYouth extends Card {

    public GauFeralYouth() {
        addEffect(EffectSlot.ON_ATTACK,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new CardsLeftGraveyardThisTurn(),
                        new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT)));
    }
}
