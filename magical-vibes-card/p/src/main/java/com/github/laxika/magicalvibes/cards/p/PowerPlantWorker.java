package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "241")
public class PowerPlantWorker extends Card {

    public PowerPlantWorker() {
        var mineWorker = new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNamedPredicate("Mine Worker"))));
        var towerWorker = new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNamedPredicate("Tower Worker"))));
        var workerAssembly = new AllConditions(List.of(mineWorker, towerWorker));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new ConditionalReplacementEffect(
                        workerAssembly,
                        new BoostSelfEffect(2, 2),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2))),
                "{3}: This creature gets +2/+2 until end of turn. If you control creatures named Mine Worker and Tower Worker, put two +1/+1 counters on this creature instead.",
                1
        ));
    }
}
