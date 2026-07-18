package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "304")
public class CelestialPrism extends Card {

    public CelestialPrism() {
        addActivatedAbility(new ActivatedAbility(
                true,                                        // requiresTap
                "{2}",                                       // manaCost
                List.of(new AwardAnyColorManaEffect()),      // effects
                "{2}, {T}: Add one mana of any color."       // description
        ));
    }
}
