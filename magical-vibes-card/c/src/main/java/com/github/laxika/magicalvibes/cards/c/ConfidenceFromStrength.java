package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GS1", collectorNumber = "35")
public class ConfidenceFromStrength extends Card {

    public ConfidenceFromStrength() {
        // Target creature gets +4/+4 and gains trample until end of turn.
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(4, 4))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
    }
}
