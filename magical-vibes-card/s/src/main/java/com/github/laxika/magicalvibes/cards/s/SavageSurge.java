package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RTR", collectorNumber = "133")
@CardRegistration(set = "THS", collectorNumber = "176")
public class SavageSurge extends Card {

    public SavageSurge() {
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(2, 2))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()));
    }
}
