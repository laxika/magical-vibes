package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttackingOrBlocking;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedUnlessAllDefendingCreaturesBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "BNG", collectorNumber = "55")
public class Tromokratis extends Card {

    public Tromokratis() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new SourceIsAttackingOrBlocking()),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new CantBeBlockedUnlessAllDefendingCreaturesBlockEffect());
    }
}
