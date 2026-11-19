package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "RNA", collectorNumber = "46")
public class PryingEyes extends Card {

    public PryingEyes() {
        addSpellEffect(new DrawCardEffect(4));
        addSpellEffect(new DiscardEffect(2, Subjects.you()));
    }
}
