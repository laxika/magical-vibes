package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "224")
@CardRegistration(set = "MMQ", collectorNumber = "213")
public class Sizzle extends Card {

    public Sizzle() {
        addSpellEffect(DealDamageEffect.to(Subjects.eachOpponent(), 3));
    }
}
