package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

public class BahamutWardenOfLight extends Card {

    private static final PermanentAllOfPredicate OTHER_CREATURES = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));

    public BahamutWardenOfLight() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, OTHER_CREATURES));
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, OTHER_CREATURES));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new DestroyTargetPermanentEffect(new PermanentTruePredicate()));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect());
    }
}
