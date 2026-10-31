package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class GiftOfTheFae extends Card {

    public GiftOfTheFae() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(2, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
    }
}
