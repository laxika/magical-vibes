package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "251")
public class ThreeTreeMascot extends Card {

    public ThreeTreeMascot() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AwardAnyColorManaEffect()),
                "{1}: Add one mana of any color. Activate only once each turn.",
                1
        ));
    }
}
