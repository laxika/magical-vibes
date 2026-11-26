package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.JumpStartCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GRN", collectorNumber = "199")
public class SonicAssault extends Card {

    public SonicAssault() {
        target(TargetFilters.creature())
                .addSpellEffect(new TapPermanentsEffect(Subjects.target()))
                .addSpellEffect(DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), 2));
        addCastingOption(new JumpStartCast());
    }
}
