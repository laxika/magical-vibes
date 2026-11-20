package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "37")
public class WojekSiren extends Card {

    public WojekSiren() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(
                        Subjects.withCreaturesSharingAColor(Subjects.target()), 1, 1));
    }
}
