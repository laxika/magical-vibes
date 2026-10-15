package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "KTK", collectorNumber = "125")
public class ValleyDasher extends Card {

    public ValleyDasher() {
        // Haste is auto-loaded from Scryfall keywords.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
