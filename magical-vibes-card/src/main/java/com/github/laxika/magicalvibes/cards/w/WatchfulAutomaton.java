package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "182")
public class WatchfulAutomaton extends Card {

    public WatchfulAutomaton() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new ScryEffect(1)),
                "{2}{U}: Scry 1."
        ));
    }
}
