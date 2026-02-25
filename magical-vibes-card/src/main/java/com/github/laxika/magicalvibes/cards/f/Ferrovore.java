package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "88")
public class Ferrovore extends Card {

    public Ferrovore() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeArtifactCost(), new BoostSelfEffect(3, 0)),
                "{R}, Sacrifice an artifact: Ferrovore gets +3/+0 until end of turn."
        ));
    }
}
