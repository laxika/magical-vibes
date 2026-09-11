package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "81")
@CardRegistration(set = "PC2", collectorNumber = "39")
public class ArcTrail extends Card {

    public ArcTrail() {
        target(2, 2)
                .addEffect(EffectSlot.SPELL, DealDividedDamageEffect.ordered(List.of(2, 1)));
    }
}
