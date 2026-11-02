package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "201")
public class ArniSlaysTheTroll extends Card {

    public ArniSlaysTheTroll() {
        addTrigger(Triggers.chapter(1), new FightTargetsEffect());
        setSagaChapterTargetGroups(1, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1),
                new SagaChapterTargetGroup(TargetFilters.creatureAnOpponentControls(), 0, 1)
        ));

        addTrigger(Triggers.chapter(2), new AwardManaEffect(ManaColor.RED));
        addTrigger(Triggers.chapter(2), PutCounterOnTargetPermanentEffect.withTargetRestriction(
                CounterType.PLUS_ONE_PLUS_ONE, 2,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                ))));

        addTrigger(Triggers.chapter(3),
                new GainLifeEffect(new GreatestPowerAmongControlled()));
    }
}
