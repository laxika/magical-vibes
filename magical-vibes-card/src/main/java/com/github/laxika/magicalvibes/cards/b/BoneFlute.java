package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "97")
public class BoneFlute extends Card {

    public BoneFlute() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new BoostAllCreaturesEffect(-1, 0)),
                "{2}, {T}: All creatures get -1/-0 until end of turn."
        ));
    }
}
