package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PlatedKilnbeast;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "178")
public class BondedHerdbeast extends Card {

    public BondedHerdbeast() {
        setBackFaceCard(new PlatedKilnbeast());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R/P}",
                List.of(new TransformSelfEffect()),
                "{4}{R/P}: Transform this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "PlatedKilnbeast";
    }
}
