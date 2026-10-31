package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageToTargetAndAddToughnessCountersEffect;

@CardRegistration(set = "5ED", collectorNumber = "57")
@CardRegistration(set = "ICE", collectorNumber = "50")
@CardRegistration(set = "ME2", collectorNumber = "33")
public class SacredBoon extends Card {

    public SacredBoon() {
        addSpellEffect(new PreventNextDamageToTargetAndAddToughnessCountersEffect(3));
    }
}
