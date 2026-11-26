package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M10", collectorNumber = "128")
public class BurningInquiry extends Card {

    public BurningInquiry() {
        addSpellEffect(new DrawCardEffect(3, Subjects.eachPlayer()));
        addSpellEffect(new DiscardEffect(3, Subjects.eachPlayer(), true));
    }
}
