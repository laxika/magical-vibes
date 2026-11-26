package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DIS", collectorNumber = "36")
@CardRegistration(set = "C13", collectorNumber = "65")
@CardRegistration(set = "CMD", collectorNumber = "67")
@CardRegistration(set = "E01", collectorNumber = "29")
public class VisionSkeins extends Card {

    public VisionSkeins() {
        addSpellEffect(new DrawCardEffect(2, Subjects.eachPlayer()));
    }
}
