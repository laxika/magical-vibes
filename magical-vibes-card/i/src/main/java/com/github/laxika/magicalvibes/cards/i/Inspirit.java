package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "9ED", collectorNumber = "22")
@CardRegistration(set = "ONS", collectorNumber = "41")
public class Inspirit extends Card {

    public Inspirit() {
        target(TargetFilters.creature()).addSpellEffect(new UntapPermanentsEffect(Subjects.target()))
                .addSpellEffect(BoostEffect.target(2, 4));
    }
}
