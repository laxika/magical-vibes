package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MSC", collectorNumber = "62")
@CardRegistration(set = "MSC", collectorNumber = "374")
public class TitaniaProudPummeler extends Card {

    public TitaniaProudPummeler() {
        // Other creatures you control have melee.
        addStaticEffect(new GrantKeywordEffect(Keyword.MELEE, Subjects.otherCreaturesYouControl()));
    }
}
