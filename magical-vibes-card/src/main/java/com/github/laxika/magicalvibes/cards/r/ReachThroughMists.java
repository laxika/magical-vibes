package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "CHK", collectorNumber = "81")
public class ReachThroughMists extends Card {

    public ReachThroughMists() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
