package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MaladyInvoker;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "189")
public class HerbologyInstructor extends Card {

    public HerbologyInstructor() {
        setBackFaceCard(new MaladyInvoker());

        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{B/P}",
                List.of(new TransformSelfEffect()),
                "{6}{B/P}: Transform this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "MaladyInvoker";
    }
}
