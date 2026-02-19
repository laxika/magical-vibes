package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "61")
public class WrathOfGod extends Card {

    public WrathOfGod() {
        addEffect(EffectSlot.SPELL, new DestroyAllCreaturesEffect(true));
    }
}
