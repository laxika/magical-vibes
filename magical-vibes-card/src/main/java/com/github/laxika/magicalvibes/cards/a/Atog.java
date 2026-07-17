package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "209")
public class Atog extends Card {

    public Atog() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeArtifactCost(), new BoostSelfEffect(2, 2)),
                "Sacrifice an artifact: This creature gets +2/+2 until end of turn."
        ));
    }
}
