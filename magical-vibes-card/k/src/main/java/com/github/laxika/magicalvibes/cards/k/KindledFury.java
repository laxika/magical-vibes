package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M10", collectorNumber = "144")
@CardRegistration(set = "MOR", collectorNumber = "92")
@CardRegistration(set = "HOU", collectorNumber = "101")
@CardRegistration(set = "M13", collectorNumber = "137")
@CardRegistration(set = "DTK", collectorNumber = "142")
@CardRegistration(set = "NEO", collectorNumber = "151")
@CardRegistration(set = "FDN", collectorNumber = "542")
public class KindledFury extends Card {

    public KindledFury() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(1, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET));
    }
}
