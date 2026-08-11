package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "237")
public class CaptainSisay extends Card {

    public CaptainSisay() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SearchLibraryEffect(new CardSupertypePredicate(CardSupertype.LEGENDARY))),
                "{T}: Search your library for a legendary card, reveal that card, put it into your hand, then shuffle."
        ));
    }
}
