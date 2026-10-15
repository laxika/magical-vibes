package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScrambleverseEffect;

@CardRegistration(set = "M12", collectorNumber = "153")
public class Scrambleverse extends Card {

    public Scrambleverse() {
        addEffect(EffectSlot.SPELL, new ScrambleverseEffect());
    }
}
