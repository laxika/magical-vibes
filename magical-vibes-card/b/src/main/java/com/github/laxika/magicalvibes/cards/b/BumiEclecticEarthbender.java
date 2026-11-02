package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "248")
public class BumiEclecticEarthbender extends Card {

    public BumiEclecticEarthbender() {
        target(TargetFilters.landYouControl())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new EarthbendTargetLandEffect(1));

        PermanentAllOfPredicate landCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentIsCreaturePredicate()));
        addTrigger(Triggers.attacks(), new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 2, landCreatures));
    }
}
