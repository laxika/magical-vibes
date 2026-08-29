package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "MMQ", collectorNumber = "64")
public class ChamberedNautilus extends Card {

    public ChamberedNautilus() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DrawCardEffect());
    }
}
