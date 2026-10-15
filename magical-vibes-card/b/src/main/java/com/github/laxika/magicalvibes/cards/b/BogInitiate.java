package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "95")
public class BogInitiate extends Card {

    public BogInitiate() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.BLACK)),
                "{1}: Add {B}."
        ));
    }
}
