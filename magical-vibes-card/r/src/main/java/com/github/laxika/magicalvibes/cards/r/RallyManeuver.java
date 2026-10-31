package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFR", collectorNumber = "36")
@CardRegistration(set = "HBG", collectorNumber = "100")
public class RallyManeuver extends Card {

    public RallyManeuver() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(2, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET));

        target(TargetFilters.creature(), 0, 1)
                .addSpellEffect(new BoostTargetCreatureEffect(0, 2))
                .addSpellEffect(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET));
    }
}
