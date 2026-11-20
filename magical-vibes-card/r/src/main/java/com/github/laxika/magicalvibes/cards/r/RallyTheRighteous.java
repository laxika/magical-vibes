package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "222")
public class RallyTheRighteous extends Card {

    public RallyTheRighteous() {
        target(TargetFilters.creature())
                .addSpellEffect(new UntapPermanentsEffect(Subjects.withCreaturesSharingAColor(Subjects.target())))
                .addSpellEffect(new BoostTargetCreatureEffect(
                        Subjects.withCreaturesSharingAColor(Subjects.target()), 2, 0));
    }
}
