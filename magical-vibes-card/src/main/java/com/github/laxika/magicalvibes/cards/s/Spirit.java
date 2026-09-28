package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1341")
@CardRegistration(set = "SLD", collectorNumber = "1852")
public class Spirit extends Card {
    public Spirit() {
        setKeywords(Set.of(Keyword.FLYING));
    }
}
