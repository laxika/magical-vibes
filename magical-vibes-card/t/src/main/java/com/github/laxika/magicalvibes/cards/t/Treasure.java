package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "153")
@CardRegistration(set = "SLD", collectorNumber = "1432")
@CardRegistration(set = "SLD", collectorNumber = "1507")
@CardRegistration(set = "SLD", collectorNumber = "1736")
@CardRegistration(set = "SLD", collectorNumber = "2065")
@CardRegistration(set = "SLD", collectorNumber = "2094")
public class Treasure extends Card {

    public Treasure() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                "{T}, Sacrifice this token: Add one mana of any color."
        ));
    }
}
