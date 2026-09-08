package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "139")
@CardRegistration(set = "DD1", collectorNumber = "11")
public class StonewoodInvoker extends Card {

    public StonewoodInvoker() {
        addActivatedAbility(new ActivatedAbility(false, "{7}{G}", List.of(new BoostSelfEffect(5, 5)),
                "{7}{G}: This creature gets +5/+5 until end of turn."));
    }
}
