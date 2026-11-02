package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "50")
public class OldFatSpiderCantSeeMe extends Card {

    public OldFatSpiderCantSeeMe() {
        addTrigger(Triggers.chapter(1),
                new GrantKeywordEffect(Set.of(Keyword.HEXPROOF), GrantScope.TARGET,
                        new PermanentIsCreaturePredicate(), GrantDuration.WHILE_SOURCE_REMAINS, null));
        setSagaChapterTargetGroups(1, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1)));

        addTrigger(Triggers.chapter(2), PreventDamageEffect.allByTargetCreatureWhileSourceRemains());
        setSagaChapterTargetGroups(2, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 1)));

        addTrigger(Triggers.chapter(3), new DrawCardEffect());
        addTrigger(Triggers.chapter(4), new DrawCardEffect());
    }
}
