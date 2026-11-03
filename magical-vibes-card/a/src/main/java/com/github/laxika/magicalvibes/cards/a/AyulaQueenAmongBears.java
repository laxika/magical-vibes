package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "155")
@CardRegistration(set = "PIP", collectorNumber = "348")
@CardRegistration(set = "PIP", collectorNumber = "876")
public class AyulaQueenAmongBears extends Card {

    public AyulaQueenAmongBears() {
        PermanentPredicate bearPredicate = new PermanentHasSubtypePredicate(CardSubtype.BEAR);
        var targetBear = new PermanentPredicateTargetFilter(bearPredicate, "Target must be a Bear");
        var targetBearYouControl = new ControlledPermanentPredicateTargetFilter(
                bearPredicate, "Target must be a Bear you control");

        addTrigger(Triggers.enters(
                        new PermanentHasSubtypePredicate(CardSubtype.BEAR),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Put two +1/+1 counters on target Bear.",
                                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                                        targetBear),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Target Bear you control fights target creature you don't control.",
                                        List.of(FightTargetsEffect.boundTargetGroupAndNext()),
                                        List.of(targetBearYouControl, TargetFilters.creatureAnOpponentControls()))
                        ))));
    }
}
