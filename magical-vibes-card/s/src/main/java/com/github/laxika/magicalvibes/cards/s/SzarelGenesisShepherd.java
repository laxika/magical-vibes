package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EOC", collectorNumber = "4")
public class SzarelGenesisShepherd extends Card {

    public SzarelGenesisShepherd() {
        addStaticEffect(new PlayLandsFromGraveyardEffect());

        PermanentPredicate anotherNontokenPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        PermanentPredicate anotherCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));

        target(new ControlledPermanentPredicateTargetFilter(anotherCreatureYouControl,
                "Target must be another creature you control"), 0, 1)
                .addTrigger(Triggers.sacrificed(
                                anotherNontokenPermanent,
                                ObjectRelation.ANOTHER_YOU_CONTROL).onlyDuringYourTurn(),
                        PutCounterOnTargetPermanentEffect.upToOneTarget(
                                CounterType.PLUS_ONE_PLUS_ONE, new SourcePower()));
    }
}
