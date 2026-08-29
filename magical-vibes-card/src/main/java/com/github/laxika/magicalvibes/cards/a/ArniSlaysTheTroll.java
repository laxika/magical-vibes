package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "201")
public class ArniSlaysTheTroll extends Card {

    public ArniSlaysTheTroll() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new FightTargetsEffect());
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1),
                new SagaChapterTargetGroup(TargetFilters.creatureAnOpponentControls(), 0, 1)
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new AwardManaEffect(ManaColor.RED));
        addEffect(EffectSlot.SAGA_CHAPTER_II, PutCounterOnTargetPermanentEffect.withTargetRestriction(
                CounterType.PLUS_ONE_PLUS_ONE, 2,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                ))));

        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new GainLifeEffect(new GreatestPowerAmongControlled()));
    }
}
