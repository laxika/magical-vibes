package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M15", collectorNumber = "91")
public class CovenantOfBlood extends Card {

    public CovenantOfBlood() {
        addSpellEffect(new DealDamageToAnyTargetEffect(4));
        addSpellEffect(new GainLifeEffect(4));
    }
}
