package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.OpponentGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;


@CardRegistration(set = "ALL", collectorNumber = "79")
@CardRegistration(set = "ME2", collectorNumber = "149")
public class RogueSkycaptain extends Card {

    public RogueSkycaptain() {
        // At the beginning of your upkeep, put a wage counter on this creature. You may pay {2}
        // for each wage counter on it. If you don't, remove all wage counters from this creature
        // and an opponent gains control of it. One sequence, so the new counter lands before the
        // payment is sized.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                PutCountersEffect.onSelf(CounterType.WAGE),
                UnlessPaysEffect.unlessYouPay(
                        PayManaCost.withGenericIncrease("{0}", new Scaled(new CountersOnSource(CounterType.WAGE), 2)),
                        SequenceEffect.of(
                                RemoveCountersEffect.allFromSelf(CounterType.WAGE),
                                new OpponentGainsControlOfSourceCreatureEffect(Duration.PERMANENT)))
        ));
    }
}
