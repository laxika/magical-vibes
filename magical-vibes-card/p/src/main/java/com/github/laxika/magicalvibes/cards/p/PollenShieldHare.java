package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HareRaising;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

@CardRegistration(set = "WOE", collectorNumber = "233")
@CardRegistration(set = "WOE", collectorNumber = "293")
public class PollenShieldHare extends Card {

    public PollenShieldHare() {
        setBackFaceCard(new HareRaising());
        addCastingOption(new AdventureCast("{G}"));

        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.creaturesYouControl(),
                new PermanentIsTokenPredicate()));
    }

    @Override
    public String getBackFaceClassName() {
        return "HareRaising";
    }
}
