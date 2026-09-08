package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

@CardRegistration(set = "RNA", collectorNumber = "154")
public class AzoriusKnightArbiter extends Card {

    public AzoriusKnightArbiter() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
