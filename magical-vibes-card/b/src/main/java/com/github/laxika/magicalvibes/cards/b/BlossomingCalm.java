package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordUntilEndOfTurnEffect;

@CardRegistration(set = "MH2", collectorNumber = "7")
public class BlossomingCalm extends Card {

    public BlossomingCalm() {
        addSpellEffect(new GrantControllerKeywordUntilEndOfTurnEffect(Keyword.HEXPROOF, Duration.UNTIL_YOUR_NEXT_TURN));
        addSpellEffect(new GainLifeEffect(2));
    }
}
