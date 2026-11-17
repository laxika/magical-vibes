package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "12")
@CardRegistration(set = "GK2", collectorNumber = "30")
public class MartyredRusalka extends Card {

    public MartyredRusalka() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(
                        new SacrificeCreatureCost(),
                        new LockTargetPermanentEffect(true, false, false, Duration.UNTIL_END_OF_TURN)
                ),
                "{W}, Sacrifice a creature: Target creature can't attack this turn.",
                TargetFilters.creature()
        ));
    }
}
