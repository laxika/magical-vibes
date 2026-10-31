package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordUntilNextTurnEffect;

@CardRegistration(set = "MH2", collectorNumber = "7")
public class BlossomingCalm extends Card {

    public BlossomingCalm() {
        addSpellEffect(new GrantControllerKeywordUntilNextTurnEffect(Keyword.HEXPROOF));
        addSpellEffect(new GainLifeEffect(2));
    }
}
