package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "KLD", collectorNumber = "16")
public class GearshiftAce extends Card {

    public GearshiftAce() {
        // Whenever this creature crews a Vehicle, that Vehicle gains first strike until end of turn.
        addEffect(EffectSlot.ON_CREWS_VEHICLE,
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TRIGGERING_PERMANENT));
    }
}
