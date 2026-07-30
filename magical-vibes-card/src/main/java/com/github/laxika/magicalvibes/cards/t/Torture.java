package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "59a")
@CardRegistration(set = "HML", collectorNumber = "59b")
@CardRegistration(set = "SHM", collectorNumber = "80")
@CardRegistration(set = "5ED", collectorNumber = "199")
public class Torture extends Card {

    public Torture() {
        target(TargetFilters.creature());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new PutCounterOnEnchantedCreatureEffect(CounterType.MINUS_ONE_MINUS_ONE)),
                "{1}{B}: Put a -1/-1 counter on enchanted creature."
        ));
    }
}
