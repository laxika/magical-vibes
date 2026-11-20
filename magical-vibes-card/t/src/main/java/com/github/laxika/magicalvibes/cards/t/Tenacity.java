package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SOI", collectorNumber = "42")
public class Tenacity extends Card {

    public Tenacity() {
        addSpellEffect(new BoostAllOwnCreaturesEffect(1, 1));
        addSpellEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.otherCreaturesYouControl()));
        addSpellEffect(new UntapPermanentsEffect(Subjects.controlledBy(Subjects.you(),
                new PermanentIsCreaturePredicate())));
    }
}
