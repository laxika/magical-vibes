package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "143")
public class AugmentingAutomaton extends Card {

    public AugmentingAutomaton() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new BoostSelfEffect(1, 1)),
                "{1}{B}: This creature gets +1/+1 until end of turn."
        ));
    }
}
