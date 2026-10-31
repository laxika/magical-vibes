package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "33")
public class RiverguardsReflexes extends Card {

    public RiverguardsReflexes() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(2, 2))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET))
                .addSpellEffect(new UntapPermanentsEffect(TapUntapScope.TARGET));
    }
}
