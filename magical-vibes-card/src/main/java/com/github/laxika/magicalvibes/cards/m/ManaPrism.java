package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "297")
public class ManaPrism extends Card {

    public ManaPrism() {
        addActivatedAbility(new ActivatedAbility(
                true,                                              // requiresTap
                null,                                              // manaCost
                List.of(new AwardManaEffect(ManaColor.COLORLESS)), // effects
                "{T}: Add {C}."                                    // description
        ));
        addActivatedAbility(new ActivatedAbility(
                true,                                        // requiresTap
                "{1}",                                       // manaCost
                List.of(new AwardAnyColorManaEffect()),      // effects
                "{1}, {T}: Add one mana of any color."       // description
        ));
    }
}
