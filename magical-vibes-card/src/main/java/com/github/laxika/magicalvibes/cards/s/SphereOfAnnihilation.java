package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceAndCreaturesAndPlaneswalkersWithManaValueAtMostSourceCountersEffect;

@CardRegistration(set = "AFR", collectorNumber = "121")
public class SphereOfAnnihilation extends Card {

    public SphereOfAnnihilation() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.VOID, new XValue()));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ExileSourceAndCreaturesAndPlaneswalkersWithManaValueAtMostSourceCountersEffect(
                        CounterType.VOID));
    }
}
