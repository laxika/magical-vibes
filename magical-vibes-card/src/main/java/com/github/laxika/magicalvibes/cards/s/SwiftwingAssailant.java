package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "DFT", collectorNumber = "32")
public class SwiftwingAssailant extends Card {

    public SwiftwingAssailant() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new MaxSpeed(),
                new StaticBoostEffect(0, 1, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new MaxSpeed(),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)));
    }
}
