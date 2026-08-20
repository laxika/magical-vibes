package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "110")
public class StandingStones extends Card {

    public StandingStones() {
        // {1}, {T}, Pay 1 life: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new PayLifeCost(1), new AwardAnyColorManaEffect()),
                "{1}, {T}, Pay 1 life: Add one mana of any color."
        ));
    }
}
