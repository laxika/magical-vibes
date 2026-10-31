package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.AdditionalLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "M20", collectorNumber = "4")
@CardRegistration(set = "ANB", collectorNumber = "1")
@CardRegistration(set = "FDC", collectorNumber = "13")
public class AngelOfVitality extends Card {

    public AngelOfVitality() {
        addStaticEffect(new AdditionalLifeGainEffect(1));
        addStaticEffect(new ConditionalEffect(
                new ControllerLifeAtLeast(25), new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}
