package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SUM", collectorNumber = "185")
@CardRegistration(set = "LEA", collectorNumber = "183")
@CardRegistration(set = "3ED", collectorNumber = "185")
@CardRegistration(set = "2ED", collectorNumber = "184")
@CardRegistration(set = "VMA", collectorNumber = "192")
@CardRegistration(set = "ME4", collectorNumber = "140")
@CardRegistration(set = "LEB", collectorNumber = "184")
public class WheelOfFortune extends Card {

    public WheelOfFortune() {
        // Each player discards their hand, then draws seven cards.
        addSpellEffect(new DiscardHandEffect(Subjects.eachPlayer()));
        addSpellEffect(new DrawCardEffect(7, Subjects.eachPlayer()));
    }
}
