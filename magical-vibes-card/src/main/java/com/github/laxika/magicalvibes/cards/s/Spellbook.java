package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;

@CardRegistration(set = "10E", collectorNumber = "343")
public class Spellbook extends Card {

    public Spellbook() {
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());
    }
}
