package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "5ED", collectorNumber = "243")
@CardRegistration(set = "4ED", collectorNumber = "205")
@CardRegistration(set = "8ED", collectorNumber = "196")
@CardRegistration(set = "7ED", collectorNumber = "198")
@CardRegistration(set = "6ED", collectorNumber = "191")
@CardRegistration(set = "DRK", collectorNumber = "71")
public class Inferno extends Card {

    public Inferno() {
        addSpellEffect(DealDamageEffect.toEachCreature(6).andTo(Subjects.eachPlayer()));
    }
}
