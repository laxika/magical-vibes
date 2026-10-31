package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NEM", collectorNumber = "84")
public class FlowstoneStrike extends Card {

    public FlowstoneStrike() {
        // Target creature gets +1/-1 and gains haste until end of turn.
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(1, -1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
