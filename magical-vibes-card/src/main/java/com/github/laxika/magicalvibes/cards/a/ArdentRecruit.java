package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "MBS", collectorNumber = "2")
public class ArdentRecruit extends Card {

    public ArdentRecruit() {
        // Metalcraft — Ardent Recruit gets +2/+2 as long as you control three or more artifacts.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new Metalcraft(), 
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}
