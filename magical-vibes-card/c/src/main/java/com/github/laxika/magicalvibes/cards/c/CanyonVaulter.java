package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "8")
public class CanyonVaulter extends Card {

    public CanyonVaulter() {
        addTrigger(Triggers.saddlesOrCrewsDuringMainPhase(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.TRIGGERING_PERMANENT));
    }
}
