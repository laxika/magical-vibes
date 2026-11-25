package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "TSP", collectorNumber = "179")
@CardRegistration(set = "MMA", collectorNumber = "133")
@CardRegistration(set = "TSR", collectorNumber = "194")
public class SuddenShock extends Card {

    public SuddenShock() {
        addSpellEffect(DealDamageEffect.toAnyTarget(2));
    }
}
