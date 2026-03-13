package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "120")
public class VisceraSeer extends Card {

    public VisceraSeer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeCreatureCost(), new ScryEffect(1)),
                "Sacrifice a creature: Scry 1."
        ));
    }
}
