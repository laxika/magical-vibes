package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "61")
public class BloodCelebrant extends Card {

    public BloodCelebrant() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new PayLifeCost(1), new AwardAnyColorManaEffect()),
                "{B}, Pay 1 life: Add one mana of any color."
        ));
    }
}
