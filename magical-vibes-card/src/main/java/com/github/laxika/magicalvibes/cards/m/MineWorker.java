package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "239")
public class MineWorker extends Card {

    public MineWorker() {
        var powerPlantWorker = new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNamedPredicate("Power Plant Worker"))));
        var towerWorker = new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNamedPredicate("Tower Worker"))));
        var workerAssembly = new AllConditions(List.of(powerPlantWorker, towerWorker));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GainLifeEffect(new FixedIfCondition(workerAssembly, 3, 1))),
                "{T}: You gain 1 life. If you control creatures named Power Plant Worker and Tower Worker, you gain 3 life instead."
        ));
    }
}
