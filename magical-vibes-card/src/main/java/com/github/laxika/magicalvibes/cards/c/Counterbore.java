package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileAllWithSameNameEffect;

@CardRegistration(set = "SHM", collectorNumber = "33")
public class Counterbore extends Card {

    public Counterbore() {
        addEffect(EffectSlot.SPELL, new CounterSpellAndExileAllWithSameNameEffect());
    }
}
