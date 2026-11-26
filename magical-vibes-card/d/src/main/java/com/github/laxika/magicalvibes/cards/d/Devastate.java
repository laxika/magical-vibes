package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PCY", collectorNumber = "87")
public class Devastate extends Card {

    public Devastate() {
        target(TargetFilters.land())
                .addSpellEffect(new DestroyTargetPermanentEffect());
        addSpellEffect(DealDamageEffect.toEachCreature(1).andTo(Subjects.eachPlayer()));
    }
}
