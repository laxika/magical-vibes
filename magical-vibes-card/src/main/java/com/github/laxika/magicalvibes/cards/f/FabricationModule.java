package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "211")
public class FabricationModule extends Card {

    public FabricationModule() {
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_CONTROLLER_GETS_ENERGY,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new EnergyCountersEffect(1)),
                "{4}, {T}: You get {E}."
        ));
    }
}
