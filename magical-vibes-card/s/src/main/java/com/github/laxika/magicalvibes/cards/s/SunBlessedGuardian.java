package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FurnaceBlessedConqueror;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "38")
public class SunBlessedGuardian extends Card {

    public SunBlessedGuardian() {
        setBackFaceCard(new FurnaceBlessedConqueror());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{R/P}",
                List.of(new TransformSelfEffect()),
                "{5}{R/P}: Transform Sun-Blessed Guardian. Activate only as a sorcery.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "FurnaceBlessedConqueror";
    }
}
