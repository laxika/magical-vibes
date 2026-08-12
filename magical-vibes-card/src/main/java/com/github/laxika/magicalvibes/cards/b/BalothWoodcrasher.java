package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "ZEN", collectorNumber = "157")
public class BalothWoodcrasher extends Card {

    public BalothWoodcrasher() {
        // Landfall — Whenever a land you control enters, this creature gets +4/+4 and gains trample until end of turn.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new BoostSelfEffect(4, 4));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF));
    }
}
