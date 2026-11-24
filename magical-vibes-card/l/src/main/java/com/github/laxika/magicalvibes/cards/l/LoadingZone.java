package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaCastingCost;

import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersEffect;
import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "196")
public class LoadingZone extends Card {

    public LoadingZone() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{G}"))));
        addStaticEffect(AddOnePlusOneCountersEffect.twice(CounterType.ANY,
                AddOnePlusOneCountersEffect.youControl(AddOnePlusOneCountersEffect.creatureOr(CardSubtype.SPACECRAFT, CardSubtype.PLANET))));
    }
}
