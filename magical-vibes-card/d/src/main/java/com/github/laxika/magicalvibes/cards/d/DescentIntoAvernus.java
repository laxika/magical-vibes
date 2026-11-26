package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "2176")
public class DescentIntoAvernus extends Card {

    public DescentIntoAvernus() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                PutCountersEffect.onSelf(CounterType.DESCENT, 2),
                new EachPlayerCreatesTokenEffect(
                        CreateTokenEffect.ofTreasureToken(new CountersOnSource(CounterType.DESCENT))),
                DealDamageEffect.to(Subjects.eachPlayer(), new CountersOnSource(CounterType.DESCENT))));
    }
}
