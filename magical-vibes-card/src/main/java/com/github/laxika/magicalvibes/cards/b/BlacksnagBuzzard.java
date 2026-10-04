package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "OTJ", collectorNumber = "79")
public class BlacksnagBuzzard extends Card {

    public BlacksnagBuzzard() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{B}"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Morbid(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1))));
    }
}
