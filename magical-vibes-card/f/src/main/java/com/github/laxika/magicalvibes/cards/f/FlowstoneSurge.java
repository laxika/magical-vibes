package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "NEM", collectorNumber = "85")
public class FlowstoneSurge extends Card {

    public FlowstoneSurge() {
        addStaticEffect(new StaticBoostEffect(1, -1, GrantScope.ALL_OWN_CREATURES));
    }
}
