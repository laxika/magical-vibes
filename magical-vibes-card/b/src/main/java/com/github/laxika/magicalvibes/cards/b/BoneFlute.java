package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "97")
@CardRegistration(set = "ME3", collectorNumber = "192")
public class BoneFlute extends Card {

    public BoneFlute() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(BoostEffect.allCreatures(-1, 0)),
                "{2}, {T}: All creatures get -1/-0 until end of turn."
        ));
    }
}
