package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersForManaEffect;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "249")
public class MageRingNetwork extends Card {

    public MageRingNetwork() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {1}, {T}: Put a storage counter on this land.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new PutCountersOnSelfEffect(CounterType.STORAGE)),
                "{1}, {T}: Put a storage counter on this land."
        ));

        // {T}, Remove any number of storage counters from this land: Add {C} for each removed.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new RemoveCountersForManaEffect(ManaColor.COLORLESS, CounterType.STORAGE)),
                "{T}, Remove any number of storage counters from this land: Add {C} for each storage counter removed this way."
        ));
    }
}
