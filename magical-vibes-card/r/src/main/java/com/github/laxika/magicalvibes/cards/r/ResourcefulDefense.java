package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.MoveAnyNumberOfCountersFromTargetPermanentToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTargetForEachLeavingSourceCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "251")
@CardRegistration(set = "NCC", collectorNumber = "19")
@CardRegistration(set = "NCC", collectorNumber = "120")
@CardRegistration(set = "EOC", collectorNumber = "67")
public class ResourcefulDefense extends Card {

    public ResourcefulDefense() {
        PutCountersOnTargetForEachLeavingSourceCountersEffect transferCounters =
                new PutCountersOnTargetForEachLeavingSourceCountersEffect(new PermanentTruePredicate());
        var targetPermanentYouControl = target(TargetFilters.permanentYouControl());
        // "A permanent you control ... if it had counters on it", this one included: one ability with two trigger
        // conditions and the same filter, judged on the permanent as it last existed (CR 603.10a).
        var hadCounters = new PermanentHasCountersPredicate(CounterType.ANY);
        targetPermanentYouControl.addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.leavesBattlefield(hadCounters, ObjectRelation.THIS)),
                new TriggerCondition(Triggers.leavesBattlefield(hadCounters, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(transferCounters))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                List.of(new MoveAnyNumberOfCountersFromTargetPermanentToTargetPermanentEffect()),
                "{4}{W}: Move any number of counters from target permanent you control onto a second target permanent you control.",
                List.of(TargetFilters.permanentYouControl(), TargetFilters.permanentYouControl()),
                2,
                2
        ));
    }
}
