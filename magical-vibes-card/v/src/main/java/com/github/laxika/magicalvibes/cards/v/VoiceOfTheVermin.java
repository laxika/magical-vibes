package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "163")
public class VoiceOfTheVermin extends Card {

    public VoiceOfTheVermin() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.SHIELD, new Fixed(1)));

        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.attacks(), new SetBasePowerToughnessEffect(4, 4));
    }
}
