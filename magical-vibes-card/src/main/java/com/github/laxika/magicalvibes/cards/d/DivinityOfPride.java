package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "EVE", collectorNumber = "86")
public class DivinityOfPride extends Card {

    public DivinityOfPride() {
        // This creature gets +4/+4 as long as you have 25 or more life. (Flying and lifelink are auto-loaded.)
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerLifeAtLeast(25), new StaticBoostEffect(4, 4, GrantScope.SELF)));
    }
}
