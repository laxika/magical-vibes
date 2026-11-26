package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "265")
public class TowerOfChampions extends Card {

    public TowerOfChampions() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{8}",
                List.of(BoostEffect.target(6, 6)),
                "{8}, {T}: Target creature gets +6/+6 until end of turn."
        ));
    }
}
