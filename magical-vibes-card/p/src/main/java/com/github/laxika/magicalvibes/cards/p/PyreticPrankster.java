package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GlisteningGoremonger;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "157")
public class PyreticPrankster extends Card {

    public PyreticPrankster() {
        setBackFaceCard(new GlisteningGoremonger());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B/P}",
                List.of(new TransformSelfEffect()),
                "{3}{B/P}: Transform this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "GlisteningGoremonger";
    }
}
