package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DMU", collectorNumber = "21")
public class JoinForces extends Card {

    public JoinForces() {
        target(TargetFilters.creature(), 0, 2)
                .addSpellEffect(new UntapPermanentsEffect(Subjects.allTargets()))
                .addSpellEffect(new BoostTargetCreatureEffect(2, 2));
    }
}
