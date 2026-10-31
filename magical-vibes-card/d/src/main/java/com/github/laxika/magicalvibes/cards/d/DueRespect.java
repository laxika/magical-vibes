package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentsEnterTappedThisTurnEffect;

@CardRegistration(set = "NPH", collectorNumber = "8")
public class DueRespect extends Card {

    public DueRespect() {
        addSpellEffect(new PermanentsEnterTappedThisTurnEffect());
        addSpellEffect(new DrawCardEffect());
    }
}
