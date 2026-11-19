package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "FRF", collectorNumber = "33")
public class EnhancedAwareness extends Card {

    public EnhancedAwareness() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new DiscardEffect(1, Subjects.you()));
    }
}
