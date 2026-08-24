package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "173")
public class PollenbrightDruid extends Card {

    public PollenbrightDruid() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on target creature",
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Proliferate",
                        new ProliferateEffect())
        )));
    }
}
