package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class OnAlert extends Card {

    public OnAlert() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 2))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()));
    }
}
