package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "BNG", collectorNumber = "18")
public class HoldAtBay extends Card {

    public HoldAtBay() {
        addSpellEffect(PreventDamageEffect.nextToTarget(7));
    }
}
