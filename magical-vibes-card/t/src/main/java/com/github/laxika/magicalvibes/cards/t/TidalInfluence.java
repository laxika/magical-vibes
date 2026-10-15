package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsWithSourceNameEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FEM", collectorNumber = "28")
@CardRegistration(set = "FEM", collectorNumber = "175")
public class TidalInfluence extends Card {

    public TidalInfluence() {
        addEffect(EffectSlot.STATIC, new CantCastSpellsWithSourceNameEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.TIDE, new Fixed(1)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.TIDE));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.TIDE),
                        new NotCondition(new SourceCounterThreshold(2, CounterType.TIDE)))),
                new StaticBoostEffect(-2, 0, GrantScope.ALL_CREATURES,
                        new PermanentColorInPredicate(Set.of(CardColor.BLUE)))));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(
                        new SourceCounterThreshold(3, CounterType.TIDE),
                        new NotCondition(new SourceCounterThreshold(4, CounterType.TIDE)))),
                new StaticBoostEffect(2, 0, GrantScope.ALL_CREATURES,
                        new PermanentColorInPredicate(Set.of(CardColor.BLUE)))));

        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                new PermanentHasAtLeastCountersPredicate(CounterType.TIDE, 4),
                List.of(new RemoveAllCountersEffect(CounterType.TIDE)),
                "Tidal Influence's state-triggered ability"));
    }
}
