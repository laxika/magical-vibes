package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "214")
public class DaiLiAgents extends Card {

    public DaiLiAgents() {
        setAllowSharedTargets(true);

        var earthbendTargetLand = new EarthbendTargetLandEffect(1);
        target(TargetFilters.landYouControl())
                .bindEffect(earthbendTargetLand);
        var earthbendTargetLand2 = new EarthbendTargetLandEffect(1);
        target(TargetFilters.landYouControl())
                .bindEffect(earthbendTargetLand2);
        addTrigger(Triggers.enters(ObjectRelation.THIS), earthbendTargetLand, earthbendTargetLand2);

        PermanentCount creaturesWithCounters = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
                )),
                CountScope.CONTROLLER);
        addTrigger(Triggers.attacks(),
                new LoseLifeEffect(creaturesWithCounters, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(creaturesWithCounters));
    }
}
