package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "INV", collectorNumber = "176")
public class TribalFlames extends Card {

    public TribalFlames() {
        addEffect(EffectSlot.SPELL,
                new DealDamageToAnyTargetEffect(new BasicLandTypesAmongControlledLands()));
    }
}
