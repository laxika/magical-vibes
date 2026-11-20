package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "176")
public class FlyingCraneTechnique extends Card {

    public FlyingCraneTechnique() {
        addSpellEffect(new UntapPermanentsEffect(Subjects.controlledBy(Subjects.you(),
                new PermanentIsCreaturePredicate())));
        addSpellEffect(new GrantKeywordEffect(
                Set.of(Keyword.FLYING, Keyword.DOUBLE_STRIKE), Subjects.otherCreaturesYouControl()));
    }
}
