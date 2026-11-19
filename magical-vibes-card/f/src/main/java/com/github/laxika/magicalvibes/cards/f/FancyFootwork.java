package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "19")
public class FancyFootwork extends Card {

    public FancyFootwork() {
        // Untap one or two target creatures. They each get +2/+2 until end of turn.
        target(TargetFilters.creature(), 1, 2)
                .addSpellEffect(new UntapPermanentsEffect(Subjects.allTargets()))
                .addSpellEffect(new BoostTargetCreatureEffect(2, 2));
    }
}
