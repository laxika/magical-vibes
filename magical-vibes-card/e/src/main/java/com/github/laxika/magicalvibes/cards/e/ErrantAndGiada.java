package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "224")
@CardRegistration(set = "MOM", collectorNumber = "306")
public class ErrantAndGiada extends Card {

    public ErrantAndGiada() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addStaticEffect(new AllowCastFromTopOfLibraryEffect(
                new CardAnyOfPredicate(List.of(
                        new CardKeywordPredicate(Keyword.FLASH),
                        new CardKeywordPredicate(Keyword.FLYING)
                ))));
    }
}
