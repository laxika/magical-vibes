package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "138")
public class ManaCylix extends Card {

    public ManaCylix() {
        addActivatedAbility(new ActivatedAbility(
                true,                                        // requiresTap
                "{1}",                                       // manaCost
                List.of(new AwardAnyColorManaEffect()),      // effects
                "{1}, {T}: Add one mana of any color."       // description
        ));
    }
}
