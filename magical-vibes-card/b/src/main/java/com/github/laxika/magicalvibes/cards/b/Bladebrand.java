package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M20", collectorNumber = "86")
@CardRegistration(set = "MID", collectorNumber = "87")
@CardRegistration(set = "DBL", collectorNumber = "87")
@CardRegistration(set = "RNA", collectorNumber = "64")
@CardRegistration(set = "RVR", collectorNumber = "68")
public class Bladebrand extends Card {

    public Bladebrand() {
        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
