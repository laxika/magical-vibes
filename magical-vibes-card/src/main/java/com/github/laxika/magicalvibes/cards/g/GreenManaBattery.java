package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersForManaEffect;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "323")
public class GreenManaBattery extends Card {

    public GreenManaBattery() {
        // {2}, {T}: Put a charge counter on this artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new PutCountersOnSelfEffect(CounterType.CHARGE)),
                "{2}, {T}: Put a charge counter on this artifact."
        ));

        // {T}, Remove any number of charge counters from this artifact: Add {G}, then add an
        // additional {G} for each charge counter removed this way. The base {G} and the per-counter
        // {G} are two mana-producing effects; the first adds the guaranteed {G}, the second prompts
        // for how many charge counters to remove and adds that much more (both marked mana abilities).
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN, 1),
                        new RemoveCountersForManaEffect(ManaColor.GREEN, CounterType.CHARGE)),
                "{T}, Remove any number of charge counters from this artifact: Add {G}, then add an additional {G} for each charge counter removed this way."
        ));
    }
}
