package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "XLN", collectorNumber = "35")
public class ShelteringLight extends Card {

    public ShelteringLight() {
        target(TargetFilters.creature()).addSpellEffect(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET));
        addSpellEffect(new ScryEffect(1));
    }
}
