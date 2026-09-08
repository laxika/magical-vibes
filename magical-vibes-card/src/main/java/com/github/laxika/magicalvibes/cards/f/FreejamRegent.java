package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "81")
public class FreejamRegent extends Card {

    public FreejamRegent() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}",
                List.of(new BoostSelfEffect(2, 0)), "{1}{R}: This creature gets +2/+0 until end of turn."));
    }
}
