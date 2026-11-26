package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "FDN", collectorNumber = "79")
public class Boltwave extends Card {

    public Boltwave() {
        addSpellEffect(DealDamageEffect.to(Subjects.eachOpponent(), 3));
    }
}
