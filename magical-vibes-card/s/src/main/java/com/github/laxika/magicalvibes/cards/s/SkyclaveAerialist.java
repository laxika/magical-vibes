package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "78")
public class SkyclaveAerialist extends Card {

    public SkyclaveAerialist() {
        setBackFaceCard(new SkyclaveInvader());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G/P}",
                List.of(new TransformSelfEffect()),
                "{4}{G/P}: Transform this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "SkyclaveInvader";
    }
}
