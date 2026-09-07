package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSelfReturnFromGraveyardWithOneFewerCounterEffect;

@CardRegistration(set = "FDN", collectorNumber = "66")
@CardRegistration(set = "FDN", collectorNumber = "321")
@CardRegistration(set = "FDN", collectorNumber = "385")
@CardRegistration(set = "FDN", collectorNumber = "462")
public class NineLivesFamiliar extends Card {

    public NineLivesFamiliar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new WasCast(),
                new EnterWithCountersEffect(CounterType.REVIVAL, new Fixed(8))));
        addEffect(EffectSlot.ON_DEATH,
                new RegisterDelayedSelfReturnFromGraveyardWithOneFewerCounterEffect(CounterType.REVIVAL));
    }
}
