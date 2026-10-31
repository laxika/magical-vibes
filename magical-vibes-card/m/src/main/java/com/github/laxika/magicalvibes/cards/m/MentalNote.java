package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "JUD", collectorNumber = "46")
@CardRegistration(set = "SLD", collectorNumber = "2154")
public class MentalNote extends Card {

    public MentalNote() {
        addSpellEffect(new MillEffect(2, MillRecipient.CONTROLLER));
        addSpellEffect(new DrawCardEffect());
    }
}
