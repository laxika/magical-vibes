package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "WAR", collectorNumber = "46")
public class ContentiousPlan extends Card {

    public ContentiousPlan() {
        addSpellEffect(new ProliferateEffect());
        addSpellEffect(new DrawCardEffect());
    }
}
