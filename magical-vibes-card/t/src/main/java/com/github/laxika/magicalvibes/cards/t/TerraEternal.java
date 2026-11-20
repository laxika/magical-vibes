package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "WWK", collectorNumber = "22")
public class TerraEternal extends Card {

    public TerraEternal() {
        addStaticEffect(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.allLands()));
    }
}
