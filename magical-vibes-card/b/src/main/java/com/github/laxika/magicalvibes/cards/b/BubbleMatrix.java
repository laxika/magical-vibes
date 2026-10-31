package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToCreaturesEffect;

@CardRegistration(set = "WTH", collectorNumber = "146")
public class BubbleMatrix extends Card {

    public BubbleMatrix() {
        // "Prevent all damage that would be dealt to creatures." — every creature, both players.
        addStaticEffect(PreventDamageToCreaturesEffect.all());
    }
}
