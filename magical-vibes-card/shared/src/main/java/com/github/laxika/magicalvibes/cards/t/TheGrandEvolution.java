package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnFrontEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndPutMilledCreaturesOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

public class TheGrandEvolution extends Card {

    public TheGrandEvolution() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new MillControllerAndPutMilledCreaturesOntoBattlefieldEffect(10, 2));

        target(TargetFilters.creatureYouControl(), 0, 99)
                .addEffect(EffectSlot.SAGA_CHAPTER_II,
                        DistributeCountersAmongTargetsEffect.chosenAmongTargetCreatures(
                                CounterType.PLUS_ONE_PLUS_ONE, new Fixed(7)));

        addEffect(EffectSlot.SAGA_CHAPTER_III, SequenceEffect.of(
                new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                "{1}",
                                List.of(new SourceFightsTargetCreatureEffect()),
                                "{1}: This creature fights target creature you don't control.",
                                TargetFilters.creatureAnOpponentControls()),
                        GrantScope.OWN_CREATURES,
                        null,
                        EffectDuration.UNTIL_END_OF_TURN),
                new ExileSelfAndReturnFrontEffect()));
    }
}
