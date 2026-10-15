package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "113")
public class OvergrownEstate extends Card {

    public OvergrownEstate() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land", false),
                        new GainLifeEffect(3)
                ),
                "Sacrifice a land: You gain 3 life."
        ));
    }
}
