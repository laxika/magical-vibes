package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "148")
@CardRegistration(set = "BRB", collectorNumber = "50")
public class PhyrexianGhoul extends Card {

    public PhyrexianGhoul() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeCreatureCost(), new BoostSelfEffect(2, 2)),
                "Sacrifice a creature: This creature gets +2/+2 until end of turn."
        ));
    }
}
