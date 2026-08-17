package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "DFT", collectorNumber = "246")
public class WalkingSarcophagus extends Card {

    public WalkingSarcophagus() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new MaxSpeed(),
                new StaticBoostEffect(1, 2, GrantScope.SELF)));
    }
}
