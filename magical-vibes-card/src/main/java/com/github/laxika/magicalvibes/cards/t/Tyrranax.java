package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "98")
public class Tyrranax extends Card {

    public Tyrranax() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}",
                List.of(new BoostSelfEffect(-1, 1)), "{1}{G}: Tyrranax gets -1/+1 until end of turn."));
    }
}
