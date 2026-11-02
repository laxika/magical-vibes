package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.i.ImperialMoth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "4")
public class BefriendingTheMoths extends Card {

    public BefriendingTheMoths() {
        setBackFaceCard(new ImperialMoth());
        addChapterEffects(1);
        addChapterEffects(2);
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    private void addChapterEffects(int chapter) {
        addTrigger(Triggers.chapter(chapter), new BoostTargetCreatureEffect(1, 1));
        addTrigger(Triggers.chapter(chapter), new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
        setSagaChapterTargetGroups(chapter, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1)));
    }

    @Override
    public String getBackFaceClassName() {
        return "ImperialMoth";
    }
}
