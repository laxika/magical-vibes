package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "231")
public class TheSuperHeroCivilWar extends Card {

    public TheSuperHeroCivilWar() {
        addTrigger(Triggers.chapter(1),
                GainControlOfTargetEffect.withTargetPredicate(
                        Duration.WHILE_SOURCE_REMAINS,
                        new PermanentIsCreaturePredicate()));
        setSagaChapterTargetGroups(1, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 2, 6)));

        addTrigger(Triggers.chapter(2),
                BoostEffect.creaturesYouControl(1, 1),
                new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.creaturesYouControl()));

        addTrigger(Triggers.chapter(3), new FightTargetsEffect());
        setSagaChapterTargetGroups(3, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1),
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 1)));
    }
}
