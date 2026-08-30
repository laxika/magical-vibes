package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerDidntLoseLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MoveDyingSourceCountersToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "12")
public class EssenceChanneler extends Card {

    public EssenceChanneler() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new ControllerDidntLoseLifeThisTurn()),
                new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.VIGILANCE), GrantScope.SELF)));
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_DEATH,
                new MoveDyingSourceCountersToTargetCreatureEffect(true));
    }
}
