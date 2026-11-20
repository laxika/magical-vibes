package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "RNA", collectorNumber = "60")
@CardRegistration(set = "ANB", collectorNumber = "42")
public class WindstormDrake extends Card {

    public WindstormDrake() {
        // Other creatures you control with flying get +1/+0.
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.otherCreaturesYouControl(),
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
