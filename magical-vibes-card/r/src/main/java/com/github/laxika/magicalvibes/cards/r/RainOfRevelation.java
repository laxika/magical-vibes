package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M21", collectorNumber = "61")
@CardRegistration(set = "MH1", collectorNumber = "65")
public class RainOfRevelation extends Card {

    public RainOfRevelation() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new DiscardEffect(1, Subjects.you()));
    }
}
