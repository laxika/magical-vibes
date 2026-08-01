package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextTurnEffect;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "28")
public class Chronatog extends Card {

    public Chronatog() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(new BoostSelfEffect(3, 3), new SkipNextTurnEffect()),
                "{0}: This creature gets +3/+3 until end of turn. You skip your next turn. Activate only once each turn.",
                1));
    }
}
