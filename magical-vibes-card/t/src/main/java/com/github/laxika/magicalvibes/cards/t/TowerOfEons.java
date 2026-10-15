package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "266")
public class TowerOfEons extends Card {

    public TowerOfEons() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{8}",
                List.of(new GainLifeEffect(10)),
                "{8}, {T}: You gain 10 life."
        ));
    }
}
