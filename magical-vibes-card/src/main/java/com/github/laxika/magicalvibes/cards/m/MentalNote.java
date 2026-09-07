package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "JUD", collectorNumber = "46")
public class MentalNote extends Card {

    public MentalNote() {
        addEffect(EffectSlot.SPELL, new MillEffect(2, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
