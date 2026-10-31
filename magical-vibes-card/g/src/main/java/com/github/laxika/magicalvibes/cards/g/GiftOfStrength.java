package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOU", collectorNumber = "117")
@CardRegistration(set = "RNA", collectorNumber = "127")
@CardRegistration(set = "THB", collectorNumber = "171")
public class GiftOfStrength extends Card {

    public GiftOfStrength() {
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(3, 3))
                .addSpellEffect(new GrantKeywordEffect(Keyword.REACH, GrantScope.TARGET));
    }
}
