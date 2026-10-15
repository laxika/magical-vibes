package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PhyrexianSkyflayer;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "143")
public class HarriedArtisan extends Card {

    public HarriedArtisan() {
        setBackFaceCard(new PhyrexianSkyflayer());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W/P}",
                List.of(new TransformSelfEffect()),
                "{3}{W/P}: Transform this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "PhyrexianSkyflayer";
    }
}
