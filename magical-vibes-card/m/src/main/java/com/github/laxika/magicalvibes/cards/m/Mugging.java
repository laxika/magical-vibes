package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GTC", collectorNumber = "102")
@CardRegistration(set = "RVR", collectorNumber = "119")
public class Mugging extends Card {

    public Mugging() {
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(2))
                .addSpellEffect(new CantBlockThisTurnEffect(Subjects.target()));
    }
}
