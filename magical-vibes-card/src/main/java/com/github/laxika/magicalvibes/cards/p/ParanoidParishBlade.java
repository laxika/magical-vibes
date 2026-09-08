package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "33")
public class ParanoidParishBlade extends Card {

    public ParanoidParishBlade() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Delirium(),
                new StaticBoostEffect(1, 0, Set.of(Keyword.FIRST_STRIKE), GrantScope.SELF)));
    }
}
