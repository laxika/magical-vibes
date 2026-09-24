package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "MRD", collectorNumber = "205")
@CardRegistration(set = "MPS", collectorNumber = "18")
@CardRegistration(set = "BRR", collectorNumber = "33")
@CardRegistration(set = "MSC", collectorNumber = "288")
@CardRegistration(set = "MSC", collectorNumber = "447")
public class MindsEye extends Card {

    public MindsEye() {
        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new MayPayManaEffect("{1}", new DrawCardEffect(1), "Pay {1} to draw a card?"));
    }
}
