package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "PTK", collectorNumber = "75")
@CardRegistration(set = "VMA", collectorNumber = "120")
@CardRegistration(set = "ME3", collectorNumber = "65")
@CardRegistration(set = "C13", collectorNumber = "77")
public class Famine extends Card {

    public Famine() {
        addSimultaneousSpellEffects(DealDamageEffect.toEachCreature(3),
                DealDamageEffect.to(Subjects.eachPlayer(), 3));
    }
}
