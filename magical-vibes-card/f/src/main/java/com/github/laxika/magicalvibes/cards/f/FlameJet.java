package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;

@CardRegistration(set = "UDS", collectorNumber = "81")
public class FlameJet extends Card {

    public FlameJet() {
        addSpellEffect(new DealDamageToTargetPlayerOrPlaneswalkerEffect(3));
        addCycling("{2}");
    }
}
