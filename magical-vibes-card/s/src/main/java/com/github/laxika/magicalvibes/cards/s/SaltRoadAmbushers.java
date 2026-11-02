package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "198")
@CardRegistration(set = "MKC", collectorNumber = "184")
public class SaltRoadAmbushers extends Card {

    public SaltRoadAmbushers() {
        addMorph("{3}{G}{G}");
        addStaticEffect(new PutCountersOnTurnFaceUpEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, false));
        addTrigger(Triggers.turnedFaceUp(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                                new PermanentIsCreaturePredicate())),
                        ObjectRelation.YOU_CONTROL),
                new PutCounterOnReferencedPermanentEffect(
                        PermanentReference.TRIGGERING,
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new Fixed(2),
                        new PermanentIsCreaturePredicate()));
    }
}
