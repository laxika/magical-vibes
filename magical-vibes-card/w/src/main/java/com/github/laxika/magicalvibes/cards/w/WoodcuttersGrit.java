package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EMN", collectorNumber = "179")
public class WoodcuttersGrit extends Card {

    public WoodcuttersGrit() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new BoostTargetCreatureEffect(3, 3))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET));
    }
}
