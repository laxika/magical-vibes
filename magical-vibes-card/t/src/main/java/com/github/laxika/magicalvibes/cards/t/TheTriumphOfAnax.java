package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "160")
public class TheTriumphOfAnax extends Card {

    public TheTriumphOfAnax() {
        addChapterPowerAndTrample(1);
        addChapterPowerAndTrample(2);
        addChapterPowerAndTrample(3);

        addTrigger(Triggers.chapter(4), new FightTargetsEffect());
        setSagaChapterTargetGroups(4, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1),
                new SagaChapterTargetGroup(TargetFilters.creatureAnOpponentControls(), 0, 1)
        ));
    }

    private void addChapterPowerAndTrample(int chapter) {
        addTrigger(Triggers.chapter(chapter),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET),
                new BoostTargetCreatureEffect(
                        new CountersOnSource(CounterType.LORE), new Fixed(0)));
    }
}
