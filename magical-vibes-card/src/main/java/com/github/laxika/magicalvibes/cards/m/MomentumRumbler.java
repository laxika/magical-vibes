package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SelfHasKeyword;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "IKO", collectorNumber = "126")
public class MomentumRumbler extends Card {

    public MomentumRumbler() {
        addEffect(EffectSlot.ON_ATTACK, new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FIRST_STRIKE)),
                new ConditionalEffect(new NotCondition(new SelfHasKeyword(Keyword.FIRST_STRIKE)),
                        new PutCountersOnSelfEffect(CounterType.FIRST_STRIKE))));
        addEffect(EffectSlot.ON_ATTACK, new TriggeringPermanentConditionalEffect(
                new PermanentHasKeywordPredicate(Keyword.FIRST_STRIKE),
                new ConditionalEffect(new SelfHasKeyword(Keyword.FIRST_STRIKE),
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF))));
    }
}
