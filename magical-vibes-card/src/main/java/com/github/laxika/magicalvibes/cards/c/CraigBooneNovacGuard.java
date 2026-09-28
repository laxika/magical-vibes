package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureUnlessControllerTakesDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfThenReflexiveEffect;

import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "100")
@CardRegistration(set = "PIP", collectorNumber = "628")
public class CraigBooneNovacGuard extends Card {

    public CraigBooneNovacGuard() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Set.of(Keyword.LIFELINK, Keyword.REACH), GrantScope.SELF));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new MinimumAttackers(2),
                        new PutCountersOnSelfThenReflexiveEffect(
                                CounterType.QUEST,
                                2,
                                new DealDamageToTargetCreatureUnlessControllerTakesDamageEffect(
                                        new CountersOnSource(CounterType.QUEST),
                                        new CountersOnSource(CounterType.QUEST)),
                                true)));
    }
}
