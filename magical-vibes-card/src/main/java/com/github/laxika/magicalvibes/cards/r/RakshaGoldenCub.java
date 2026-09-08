package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "5DN", collectorNumber = "12")
public class RakshaGoldenCub extends Card {

    public RakshaGoldenCub() {
        PermanentHasSubtypePredicate cats = new PermanentHasSubtypePredicate(CardSubtype.CAT);
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new Equipped(),
                new StaticBoostEffect(2, 2, GrantScope.ALL_OWN_CREATURES, cats)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new Equipped(),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.ALL_OWN_CREATURES, cats)));
    }
}
