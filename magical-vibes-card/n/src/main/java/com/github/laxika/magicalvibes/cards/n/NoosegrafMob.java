package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "98")
@CardRegistration(set = "SIR", collectorNumber = "126")
public class NoosegrafMob extends Card {

    public NoosegrafMob() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(5)));
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                new RemoveCounterFromSourceThenEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, CreateTokenEffect.blackZombie(1),
                        false, false));
    }
}
