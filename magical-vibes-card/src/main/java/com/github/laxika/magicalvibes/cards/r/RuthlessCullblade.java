package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnOpponentLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "WWK", collectorNumber = "65")
public class RuthlessCullblade extends Card {

    public RuthlessCullblade() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AnOpponentLifeAtMost(10),
                new StaticBoostEffect(2, 1, GrantScope.SELF)));
    }
}
