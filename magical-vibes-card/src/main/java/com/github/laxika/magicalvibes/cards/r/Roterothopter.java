package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "109")
public class Roterothopter extends Card {

    public Roterothopter() {
        // {2}: This creature gets +1/+0 until end of turn. Activate no more than twice each turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new BoostSelfEffect(1, 0)),
                "{2}: Roterothopter gets +1/+0 until end of turn. Activate no more than twice each turn.",
                2
        ));
    }
}
