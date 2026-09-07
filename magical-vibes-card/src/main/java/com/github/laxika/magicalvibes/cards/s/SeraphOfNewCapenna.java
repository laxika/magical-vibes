package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "36")
public class SeraphOfNewCapenna extends Card {

    public SeraphOfNewCapenna() {
        setBackFaceCard(new SeraphOfNewPhyrexia());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B/P}",
                List.of(new TransformSelfEffect()),
                "{4}{B/P}: Transform this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "SeraphOfNewPhyrexia";
    }
}
