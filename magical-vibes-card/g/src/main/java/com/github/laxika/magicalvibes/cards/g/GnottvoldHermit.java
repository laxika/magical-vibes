package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.ChromeHostHulk;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "188")
public class GnottvoldHermit extends Card {

    public GnottvoldHermit() {
        setBackFaceCard(new ChromeHostHulk());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U/P}",
                List.of(new TransformSelfEffect()),
                "{5}{U/P}: Transform this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ChromeHostHulk";
    }
}
