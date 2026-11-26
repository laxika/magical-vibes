package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class BahamutWardenOfLight extends Card {

    private static final PermanentAllOfPredicate OTHER_CREATURES = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));

    public BahamutWardenOfLight() {
        addTrigger(Triggers.chapter(1),
                PutCountersEffect.onEachYouControl(CounterType.PLUS_ONE_PLUS_ONE, 1, OTHER_CREATURES),
                new GrantKeywordEffect(Keyword.FLYING, Subjects.otherCreaturesYouControl()));
        addTrigger(Triggers.chapter(2),
                PutCountersEffect.onEachYouControl(CounterType.PLUS_ONE_PLUS_ONE, 1, OTHER_CREATURES),
                new GrantKeywordEffect(Keyword.FLYING, Subjects.otherCreaturesYouControl()));
        addTrigger(Triggers.chapter(3),
                new DestroyTargetPermanentEffect(new PermanentTruePredicate()),
                new ExileSelfAndReturnTransformedEffect());
    }
}
