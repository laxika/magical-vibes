package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MinimumSpellCostEffect;

@CardRegistration(set = "DST", collectorNumber = "154")
public class Trinisphere extends Card {

    public Trinisphere() {
        addEffect(EffectSlot.STATIC, new MinimumSpellCostEffect(3));
    }
}
