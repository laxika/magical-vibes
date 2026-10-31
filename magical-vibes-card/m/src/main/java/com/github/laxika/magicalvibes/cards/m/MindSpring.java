package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "64")
@CardRegistration(set = "MOR", collectorNumber = "41")
@CardRegistration(set = "DPA", collectorNumber = "12")
@CardRegistration(set = "DDT", collectorNumber = "14")
@CardRegistration(set = "C20", collectorNumber = "116")
public class MindSpring extends Card {

    public MindSpring() {
        addSpellEffect(new DrawCardEffect(new XValue()));
    }
}
