package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "84")
public class RakdosTrumpeter extends Card {

    public RakdosTrumpeter() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new BoostSelfEffect(2, 0)),
                "{3}{R}: This creature gets +2/+0 until end of turn."));
    }
}
