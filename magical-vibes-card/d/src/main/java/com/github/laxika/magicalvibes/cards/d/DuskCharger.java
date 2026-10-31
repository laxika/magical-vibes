package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "RIX", collectorNumber = "69")
public class DuskCharger extends Card {

    public DuskCharger() {
        addStaticEffect(new AscendEffect());
        addStaticEffect(new ConditionalEffect(new ControllerHasCityBlessing(),
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}
