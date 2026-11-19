package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ARB", collectorNumber = "111")
public class DrasticRevelation extends Card {

    public DrasticRevelation() {
        addSpellEffect(new DiscardHandEffect());
        addSpellEffect(new DrawCardEffect(7));
        addSpellEffect(new DiscardEffect(3, Subjects.you(), true));
    }
}
