package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class SummonEsperMaduin extends Card {

    private static final PermanentAllOfPredicate OTHER_CREATURES = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

    public SummonEsperMaduin() {
        addTrigger(Triggers.chapter(1),
                new RevealTopCardMatchingToHandEffect(new CardIsPermanentPredicate()));
        addTrigger(Triggers.chapter(2), new AwardManaEffect(ManaColor.GREEN, 2));
        addTrigger(Triggers.chapter(3),
                new BoostAllOwnCreaturesEffect(2, 2, OTHER_CREATURES),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES, OTHER_CREATURES));
    }
}
