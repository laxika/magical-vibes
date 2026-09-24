package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "170")
@CardRegistration(set = "MH2", collectorNumber = "90")
public class LegionVanguard extends Card {

    public LegionVanguard() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new ExploreEffect()
                ),
                "{1}, Sacrifice another creature: This creature explores."
        ));
    }
}
