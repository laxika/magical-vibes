package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "134")
@CardRegistration(set = "TMT", collectorNumber = "274")
public class TransdimensionalBovine extends Card {

    public TransdimensionalBovine() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(2)),
                "{T}: Add two mana of any one color."
        ));
    }
}
