package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "DTK", collectorNumber = "77")
public class SilumgarSpellEater extends Card {

    public SilumgarSpellEater() {
        addMorph("{4}{U}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new CounterUnlessPaysEffect(3));
    }
}
