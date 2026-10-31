package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "9")
@CardRegistration(set = "VMA", collectorNumber = "33")
@CardRegistration(set = "ME3", collectorNumber = "16")
@CardRegistration(set = "A25", collectorNumber = "20")
@CardRegistration(set = "C13", collectorNumber = "16")
public class KongmingSleepingDragon extends Card {

    public KongmingSleepingDragon() {
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES));
    }
}
