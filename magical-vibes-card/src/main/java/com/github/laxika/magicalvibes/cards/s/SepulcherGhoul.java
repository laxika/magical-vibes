package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "118")
public class SepulcherGhoul extends Card {

    public SepulcherGhoul() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "another creature"),
                        new BoostSelfEffect(2, 2)
                ),
                "Sacrifice another creature: This creature gets +2/+2 until end of turn. Activate only once each turn.",
                1
        ));
    }
}
