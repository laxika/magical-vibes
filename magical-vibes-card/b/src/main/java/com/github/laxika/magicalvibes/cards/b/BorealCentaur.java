package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "104")
public class BorealCentaur extends Card {

    public BorealCentaur() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{S}",
                List.of(BoostEffect.self(1, 1)),
                "{S}: This creature gets +1/+1 until end of turn. Activate only once each turn.",
                1
        ));
    }
}
