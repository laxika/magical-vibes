package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

public class SummonEsperMaduin extends Card {

    private static final PermanentAllOfPredicate OTHER_CREATURES = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

    public SummonEsperMaduin() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new RevealTopCardMatchingToHandEffect(new CardIsPermanentPredicate()));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new AwardManaEffect(ManaColor.GREEN, 2));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new BoostAllOwnCreaturesEffect(2, 2, OTHER_CREATURES));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES, OTHER_CREATURES));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.FINALITY, new Fixed(1)));
    }
}
