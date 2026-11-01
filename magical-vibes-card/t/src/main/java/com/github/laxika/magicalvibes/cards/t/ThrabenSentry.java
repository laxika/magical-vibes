package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "38")
public class ThrabenSentry extends Card {

    public ThrabenSentry() {
        setBackFaceCard(new ThrabenMilitia());

        // Whenever another creature you control dies, you may transform Thraben Sentry.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new MayEffect(
                new TransformSelfEffect(),
                "Transform Thraben Sentry?"
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ThrabenMilitia";
    }
}
