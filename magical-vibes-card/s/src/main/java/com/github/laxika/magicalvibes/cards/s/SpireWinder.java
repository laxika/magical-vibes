package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "RIX", collectorNumber = "57")
public class SpireWinder extends Card {

    public SpireWinder() {
        addStaticEffect(new AscendEffect());
        addStaticEffect(new ConditionalEffect(new ControllerHasCityBlessing(),
                new StaticBoostEffect(1, 1, GrantScope.SELF)));
    }
}
