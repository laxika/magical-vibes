package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "SLD", collectorNumber = "2176")
public class DescentIntoAvernus extends Card {

    public DescentIntoAvernus() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.DESCENT, 2),
                new EachPlayerCreatesTokenEffect(
                        CreateTokenEffect.ofTreasureToken(new CountersOnSource(CounterType.DESCENT))),
                new DealDamageToPlayersEffect(
                        new CountersOnSource(CounterType.DESCENT), DamageRecipient.EACH_PLAYER)));
    }
}
