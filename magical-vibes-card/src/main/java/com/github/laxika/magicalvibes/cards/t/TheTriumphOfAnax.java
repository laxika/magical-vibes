package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "160")
public class TheTriumphOfAnax extends Card {

    public TheTriumphOfAnax() {
        addChapterPowerAndTrample(EffectSlot.SAGA_CHAPTER_I);
        addChapterPowerAndTrample(EffectSlot.SAGA_CHAPTER_II);
        addChapterPowerAndTrample(EffectSlot.SAGA_CHAPTER_III);

        addEffect(EffectSlot.SAGA_CHAPTER_IV, new FightTargetsEffect());
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_IV, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1),
                new SagaChapterTargetGroup(TargetFilters.creatureAnOpponentControls(), 0, 1)
        ));
    }

    private void addChapterPowerAndTrample(EffectSlot chapter) {
        addEffect(chapter, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
        addEffect(chapter, new BoostTargetCreatureEffect(
                new CountersOnSource(CounterType.LORE), new Fixed(0)));
    }
}
