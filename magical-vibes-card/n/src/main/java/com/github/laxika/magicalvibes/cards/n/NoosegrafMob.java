package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "98")
@CardRegistration(set = "SIR", collectorNumber = "126")
public class NoosegrafMob extends Card {

    public NoosegrafMob() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(5)));
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                SequenceEffect.of(
                        new RemoveCounterFromSourceEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        new ConditionalEffect(HappenedThisWay.removedCounters(), CreateTokenEffect.blackZombie(1))));
    }
}
