package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "46")
public class MorgueToad extends Card {

    public MorgueToad() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new AwardManaEffect(ManaColor.BLUE),
                        new AwardManaEffect(ManaColor.RED)
                ),
                "Sacrifice this creature: Add {U}{R}."
        ));
    }
}
