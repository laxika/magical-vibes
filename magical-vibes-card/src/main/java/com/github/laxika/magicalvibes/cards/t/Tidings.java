package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "116")
@CardRegistration(set = "9ED", collectorNumber = "106")
@CardRegistration(set = "S99", collectorNumber = "54")
public class Tidings extends Card {

    public Tidings() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(4));
    }
}
