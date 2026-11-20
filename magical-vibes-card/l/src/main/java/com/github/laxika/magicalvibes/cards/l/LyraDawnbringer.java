package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "26")
@CardRegistration(set = "DMR", collectorNumber = "13")
@CardRegistration(set = "DMR", collectorNumber = "266")
@CardRegistration(set = "FDC", collectorNumber = "38")
public class LyraDawnbringer extends Card {

    public LyraDawnbringer() {
        addStaticEffect(new StaticBoostEffect(1, 1, Set.of(Keyword.LIFELINK), Subjects.otherCreaturesYouControl(),
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ANGEL))));
    }
}
