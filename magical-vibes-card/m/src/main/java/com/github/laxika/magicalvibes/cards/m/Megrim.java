package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "7ED", collectorNumber = "146")
@CardRegistration(set = "8ED", collectorNumber = "143")
@CardRegistration(set = "10E", collectorNumber = "157")
@CardRegistration(set = "9ED", collectorNumber = "144")
@CardRegistration(set = "M10", collectorNumber = "104")
@CardRegistration(set = "STH", collectorNumber = "62")
@CardRegistration(set = "DPA", collectorNumber = "26")
public class Megrim extends Card {

    public Megrim() {
        addTrigger(Triggers.opponentDiscardsCard(),
                DealDamageEffect.to(Subjects.triggeringPlayer(), 2));
    }
}
