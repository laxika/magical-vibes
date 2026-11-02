package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MoveDyingSourceCountersToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SurvivalTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "26")
public class ReluctantRoleModel extends Card {

    public ReluctantRoleModel() {
        addTrigger(Triggers.postcombatMainPhase(ActivePlayer.YOU), new SurvivalTriggerEffect(new ConditionalEffect(
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
        // Whenever this creature or another creature you control dies, if it had counters on it, ...
        // "If it had counters on it" is about whichever creature died, this one included: one ability with two trigger
        // conditions and the same filter, not a "this or another" relation, whose source matches whatever it was.
        deathTrigger.addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.dies(
                                new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.creatureDies(
                                new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new MoveDyingSourceCountersToTargetCreatureEffect()))));
    }
}
