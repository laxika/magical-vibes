package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ZNR", collectorNumber = "59")
public class GlacialGrasp extends Card {

    public GlacialGrasp() {
        target(TargetFilters.creature())
                .addSpellEffect(new TapPermanentsEffect(Subjects.target()))
                .addSpellEffect(new MillEffect(2, Subjects.controllerOf(Subjects.target())))
                .addSpellEffect(new SkipNextUntapEffect(TapUntapScope.TARGET))
                .addSpellEffect(new DrawCardEffect());
    }
}
