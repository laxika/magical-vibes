package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "75")
public class TimeWarp extends Card {

    public TimeWarp() {
        addEffect(EffectSlot.SPELL, new ExtraTurnEffect(1));
    }
}
