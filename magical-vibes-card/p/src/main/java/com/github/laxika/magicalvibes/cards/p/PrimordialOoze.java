package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "261")
@CardRegistration(set = "CHR", collectorNumber = "54")
@CardRegistration(set = "LEG", collectorNumber = "160")
public class PrimordialOoze extends Card {

    public PrimordialOoze() {
        // This creature attacks each combat if able.
        addStaticEffect(new MustAttackEffect());

        // At the beginning of your upkeep, put a +1/+1 counter on this creature. Then you may pay
        // {X}, where X is the number of +1/+1 counters on it. If you don't, tap this creature and
        // it deals X damage to you.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                SequenceEffect.of(
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                        UnlessPaysEffect.unlessYouPay(
                                PayManaCost.withGenericIncrease("{0}", new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)),
                                SequenceEffect.of(
                                        new TapPermanentsEffect(Subjects.source()),
                                        DealDamageEffect.to(Subjects.you(),
                                                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE))))));
    }
}
