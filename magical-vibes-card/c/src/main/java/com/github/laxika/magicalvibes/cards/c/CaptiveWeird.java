package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "49")
public class CaptiveWeird extends Card {

    public CaptiveWeird() {
        setBackFaceCard(new CompleatedConjurer());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R/P}",
                List.of(new TransformSelfEffect()),
                "{3}{R/P}: Transform this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "CompleatedConjurer";
    }
}
