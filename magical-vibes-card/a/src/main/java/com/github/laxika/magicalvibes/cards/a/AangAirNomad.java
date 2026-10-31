package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "TLE", collectorNumber = "210")
@CardRegistration(set = "TLE", collectorNumber = "265")
public class AangAirNomad extends Card {

    public AangAirNomad() {
        addStaticEffect(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES));
    }
}
