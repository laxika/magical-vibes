package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "RAV", collectorNumber = "138")
public class RainOfEmbers extends Card {

    public RainOfEmbers() {
        addSpellEffect(DealDamageEffect.toEachCreature(1).andTo(Subjects.eachPlayer()));
    }
}
