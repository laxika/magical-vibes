package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "MSC", collectorNumber = "647")
public class ArmedAssailant extends Card {

    public ArmedAssailant() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new Equipped(),
                new StaticBoostEffect(2, 0, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new Equipped(),
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)));
    }
}
