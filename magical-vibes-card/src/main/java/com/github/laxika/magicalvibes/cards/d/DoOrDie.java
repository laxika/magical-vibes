package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SeparateCreaturesIntoPilesAndDestroyEffect;

@CardRegistration(set = "INV", collectorNumber = "102")
public class DoOrDie extends Card {

    public DoOrDie() {
        addEffect(EffectSlot.SPELL, new SeparateCreaturesIntoPilesAndDestroyEffect());
    }
}
