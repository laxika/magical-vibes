package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "82")
public class CutthroatManeuver extends Card {

    public CutthroatManeuver() {
        // Up to two target creatures each get +1/+1 and gain lifelink until end of turn.
        target(TargetFilters.creature(), 0, 2)
                .addSpellEffect(new BoostTargetCreatureEffect(1, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET));
    }
}
