package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.AdditionalLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "STX", collectorNumber = "134")
public class HonorTroll extends Card {

    public HonorTroll() {
        addStaticEffect(new AdditionalLifeGainEffect(1));
        addStaticEffect(new ConditionalEffect(
                new ControllerLifeAtLeast(25), new StaticBoostEffect(2, 1, GrantScope.SELF)));
    }
}
