package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "231")
public class TheSuperHeroCivilWar extends Card {

    public TheSuperHeroCivilWar() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                GainControlOfTargetEffect.withTargetPredicate(
                        ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD,
                        new PermanentIsCreaturePredicate()));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 2, 6)));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new BoostAllOwnCreaturesEffect(1, 1));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new FightTargetsEffect());
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_III, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1),
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 1)));
    }
}
