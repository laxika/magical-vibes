package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "214")
public class DaiLiAgents extends Card {

    public DaiLiAgents() {
        setAllowSharedTargets(true);

        target(TargetFilters.landYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EarthbendTargetLandEffect(1));
        target(TargetFilters.landYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EarthbendTargetLandEffect(1));

        PermanentCount creaturesWithCounters = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
                )),
                CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ATTACK,
                new LoseLifeEffect(creaturesWithCounters, LoseLifeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ATTACK, new GainLifeEffect(creaturesWithCounters));
    }
}
