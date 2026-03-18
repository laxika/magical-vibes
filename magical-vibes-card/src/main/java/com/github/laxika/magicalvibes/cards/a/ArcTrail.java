package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "81")
public class ArcTrail extends Card {

    public ArcTrail() {
        target(null, 2, 2)
                .addEffect(EffectSlot.SPELL, new DealOrderedDamageToAnyTargetsEffect(List.of(2, 1)));
    }
}
