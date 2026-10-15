package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalSubtypeSourceDamageEffect;

import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "158")
public class ValleyFlamecaller extends Card {

    public ValleyFlamecaller() {
        addEffect(EffectSlot.STATIC, new AdditionalSubtypeSourceDamageEffect(1, Set.of(
                CardSubtype.LIZARD,
                CardSubtype.MOUSE,
                CardSubtype.OTTER,
                CardSubtype.RACCOON)));
    }
}
