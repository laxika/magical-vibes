package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "126")
public class ZimoneParadoxSculptor extends Card {

    public ZimoneParadoxSculptor() {
        PermanentPredicateTargetFilter targetCreatureYouControl = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                "Target must be a creature you control"
        );
        target(targetCreatureYouControl, 0, 2)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        PermanentPredicateTargetFilter targetCreatureOrArtifactYouControl = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsArtifactPredicate()
                        ))
                )),
                "Target must be a creature or artifact you control"
        );
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{U}",
                List.of(new DoubleCountersOnTargetPermanentEffect()),
                "{G}{U}, {T}: Double the number of each kind of counter on up to two target creatures and/or artifacts you control.",
                targetCreatureOrArtifactYouControl, null, null, null,
                List.of(), 0, 2
        ));
    }
}
