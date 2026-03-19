package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "ISD", collectorNumber = "38")
public class ThrabenSentry extends Card {

    public ThrabenSentry() {
        // Set up back face
        ThrabenMilitia backFace = new ThrabenMilitia();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // Whenever another creature you control dies, you may transform Thraben Sentry.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new MayEffect(
                new TransformSelfEffect(),
                "Transform Thraben Sentry?"
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ThrabenMilitia";
    }
}
