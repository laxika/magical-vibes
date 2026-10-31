package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "ONE", collectorNumber = "77")
public class VivisurgeonsInsight extends Card {

    public VivisurgeonsInsight() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new ProliferateEffect());
    }
}
