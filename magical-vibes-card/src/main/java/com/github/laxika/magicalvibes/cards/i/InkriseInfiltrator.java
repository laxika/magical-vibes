package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "100")
public class InkriseInfiltrator extends Card {

    public InkriseInfiltrator() {
        addActivatedAbility(new ActivatedAbility(false, "{3}{B}",
                List.of(new BoostSelfEffect(2, 2)),
                "{3}{B}: This creature gets +2/+2 until end of turn."));
    }
}
