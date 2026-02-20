package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.effect.w.WarpWorldEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;

@CardRegistration(set = "10E", collectorNumber = "248")
public class WarpWorld extends Card {

    public WarpWorld() {
        addEffect(EffectSlot.SPELL, new WarpWorldEffect());
    }
}
