package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "PLS", collectorNumber = "130")
public class UrzasGuilt extends Card {

    public UrzasGuilt() {
        addSpellEffect(new DrawCardEffect(2, Subjects.eachPlayer()));
        addSpellEffect(new DiscardEffect(3, Subjects.eachPlayer()));
        addSpellEffect(new LoseLifeEffect(4, Subjects.eachPlayer()));
    }
}
