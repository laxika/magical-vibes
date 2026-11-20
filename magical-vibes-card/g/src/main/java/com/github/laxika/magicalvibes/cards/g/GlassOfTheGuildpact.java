package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;

@CardRegistration(set = "RNA", collectorNumber = "233")
public class GlassOfTheGuildpact extends Card {

    public GlassOfTheGuildpact() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.creaturesYouControl(),
                new PermanentIsMulticoloredPredicate()));
    }
}
