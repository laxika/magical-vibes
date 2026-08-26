package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "231")
public class SignpostScarecrow extends Card {

    public SignpostScarecrow() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new AwardAnyColorManaEffect()),
                "{2}: Add one mana of any color."
        ));
    }
}
