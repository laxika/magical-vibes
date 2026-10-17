package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "23")
public class MonicaRambeau extends Card {

    public MonicaRambeau() {
        setBackFaceCard(new PhotonLivingLight());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}{W}{W}",
                List.of(new TransformSelfEffect()),
                "{2}{R}{W}{W}: Transform Monica Rambeau. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "PhotonLivingLight";
    }
}
