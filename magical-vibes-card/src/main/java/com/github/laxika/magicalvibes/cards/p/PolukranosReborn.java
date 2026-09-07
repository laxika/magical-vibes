package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "200")
public class PolukranosReborn extends Card {

    public PolukranosReborn() {
        setBackFaceCard(new PolukranosEngineOfRuin());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{W/P}",
                List.of(new TransformSelfEffect()),
                "{6}{W/P}: Transform Polukranos Reborn. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "PolukranosEngineOfRuin";
    }
}
