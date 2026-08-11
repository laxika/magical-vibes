package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "127")
public class BoulderDash extends Card {

    public BoulderDash() {
        target(2, 2)
                .addEffect(EffectSlot.SPELL, DealDividedDamageEffect.ordered(List.of(2, 1)));
    }
}
