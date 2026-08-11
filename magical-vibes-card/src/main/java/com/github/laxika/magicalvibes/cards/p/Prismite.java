package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "235")
public class Prismite extends Card {

    public Prismite() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new AwardAnyColorManaEffect()),
                "{2}: Add one mana of any color."
        ));
    }
}
