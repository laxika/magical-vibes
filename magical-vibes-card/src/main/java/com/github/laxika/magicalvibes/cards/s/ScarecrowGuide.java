package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "250")
public class ScarecrowGuide extends Card {

    public ScarecrowGuide() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AwardAnyColorManaEffect()),
                "{1}: Add one mana of any color. Activate only once each turn.",
                1
        ));
    }
}
