package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersForManaEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "276")
public class MoltenSlagheap extends Card {

    public MoltenSlagheap() {
        // {T}: Add {C}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));

        // {1}, {T}: Put a storage counter on this land.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new PutCountersOnSelfEffect(CounterType.STORAGE)),
                "{1}, {T}: Put a storage counter on this land."
        ));

        // {1}, Remove X storage counters from this land: Add X mana in any combination of {B} and/or {R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new RemoveCountersForManaEffect(
                        List.of(ManaColor.BLACK, ManaColor.RED), CounterType.STORAGE)),
                "{1}, Remove X storage counters from this land: Add X mana in any combination of {B} and/or {R}."
        ));
    }
}
