package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "200")
public class VoraciousHydra extends Card {

    public VoraciousHydra() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Double the number of +1/+1 counters on this creature",
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE,
                                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE))),
                new ChooseOneEffect.ChooseOneOption(
                        "This creature fights target creature you don't control",
                        new EnteringCreatureFightsTargetCreatureEffect(),
                        TargetFilters.creatureAnOpponentControls())
        )));
    }
}
