package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersForManaEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "321")
public class MercadianBazaar extends Card {

    public MercadianBazaar() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCountersOnSelfEffect(CounterType.STORAGE)),
                "{T}: Put a storage counter on this land."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new RemoveCountersForManaEffect(ManaColor.RED, CounterType.STORAGE)),
                "{T}, Remove any number of storage counters from this land: Add {R} for each storage counter removed this way."
        ));
    }
}
