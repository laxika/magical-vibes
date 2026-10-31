package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WTH", collectorNumber = "16")
@CardRegistration(set = "JUD", collectorNumber = "13")
@CardRegistration(set = "IMA", collectorNumber = "23")
public class GuidedStrike extends Card {

    public GuidedStrike() {
        // Target creature gets +1/+0 and gains first strike until end of turn.
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(1, 0))
          .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET))
          // Draw a card.
          .addSpellEffect(new DrawCardEffect(1));
    }
}
