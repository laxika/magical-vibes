package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;

@CardRegistration(set = "BLB", collectorNumber = "128")
public class BrazenCollector extends Card {

    public BrazenCollector() {
        addEffect(EffectSlot.ON_ATTACK,
                new AwardPersistentManaEffect(ManaColor.RED, new Fixed(1)));
    }
}
