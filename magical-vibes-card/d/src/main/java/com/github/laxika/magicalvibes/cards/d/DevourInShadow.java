package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5DN", collectorNumber = "46")
public class DevourInShadow extends Card {

    public DevourInShadow() {
        target(TargetFilters.creature())
                .addSpellEffect(new DestroyTargetPermanentEffect(true))
                        .addSpellEffect(new LoseLifeEffect(new TargetToughness(), Subjects.you()));
    }
}
