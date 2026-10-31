package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ArcaneEndeavorEffect;

@CardRegistration(set = "AFC", collectorNumber = "14")
public class ArcaneEndeavor extends Card {

    public ArcaneEndeavor() {
        addSpellEffect(new ArcaneEndeavorEffect());
    }
}
