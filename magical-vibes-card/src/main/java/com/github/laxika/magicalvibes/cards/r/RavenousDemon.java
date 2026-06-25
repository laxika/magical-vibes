package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.ArchdemonOfGreed;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "71")
public class RavenousDemon extends Card {

    public RavenousDemon() {
        ArchdemonOfGreed backFace = new ArchdemonOfGreed();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSubtypeCreatureCost(CardSubtype.HUMAN), new TransformSelfEffect()),
                "Sacrifice a Human: Transform Ravenous Demon. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ArchdemonOfGreed";
    }
}
