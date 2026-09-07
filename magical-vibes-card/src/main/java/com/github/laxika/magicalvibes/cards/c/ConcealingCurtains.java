package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RevealingEye;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "101")
public class ConcealingCurtains extends Card {

    public ConcealingCurtains() {
        setBackFaceCard(new RevealingEye());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new TransformSelfEffect()),
                "{2}{B}: Transform this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "RevealingEye";
    }
}
