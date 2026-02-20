package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "186")
public class VampireBats extends Card {

    public VampireBats() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new BoostSelfEffect(1, 0)),
                false,
                "{B}: Vampire Bats gets +1/+0 until end of turn. Activate this ability no more than twice each turn.",
                2
        ));
    }
}
