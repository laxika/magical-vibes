package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MoveDyingSourceCountersToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SurvivalTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "26")
public class ReluctantRoleModel extends Card {

    public ReluctantRoleModel() {
        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED, new SurvivalTriggerEffect(new ConditionalEffect(
                new SourceIsTapped(),
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("Put a flying counter on this creature",
                                new PutCountersOnSelfEffect(CounterType.FLYING)),
                        new ChooseOneEffect.ChooseOneOption("Put a lifelink counter on this creature",
                                new PutCountersOnSelfEffect(CounterType.LIFELINK)),
                        new ChooseOneEffect.ChooseOneOption("Put a +1/+1 counter on this creature",
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))
                )))));

        var deathTrigger = target(TargetFilters.creature(), 0, 1);
        deathTrigger.addEffect(EffectSlot.ON_DEATH,
                new MoveDyingSourceCountersToTargetCreatureEffect());
        deathTrigger.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.ANY),
                        new MoveDyingSourceCountersToTargetCreatureEffect()));
    }
}
