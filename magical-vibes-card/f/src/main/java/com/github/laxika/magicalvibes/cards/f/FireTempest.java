package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "POR", collectorNumber = "128")
@CardRegistration(set = "S99", collectorNumber = "97")
@CardRegistration(set = "ME4", collectorNumber = "114")
public class FireTempest extends Card {

    public FireTempest() {
        addSpellEffect(DealDamageEffect.toEachCreature(6).andTo(Subjects.eachPlayer()));
    }
}
