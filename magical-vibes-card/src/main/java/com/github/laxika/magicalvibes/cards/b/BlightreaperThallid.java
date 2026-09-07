package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "92")
public class BlightreaperThallid extends Card {

    public BlightreaperThallid() {
        setBackFaceCard(new BlightsowerThallid());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G/P}",
                List.of(new TransformSelfEffect()),
                "{3}{G/P}: Transform this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "BlightsowerThallid";
    }
}
