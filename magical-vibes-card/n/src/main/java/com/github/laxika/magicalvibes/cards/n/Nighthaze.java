package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "118")
public class Nighthaze extends Card {

    public Nighthaze() {
        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(Keyword.SWAMPWALK, GrantScope.TARGET));
        addSpellEffect(new DrawCardEffect());
    }
}
