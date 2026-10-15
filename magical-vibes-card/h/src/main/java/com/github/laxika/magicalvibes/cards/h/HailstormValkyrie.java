package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "97")
public class HailstormValkyrie extends Card {

    public HailstormValkyrie() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{S}{S}",
                List.of(new BoostSelfEffect(2, 2)),
                "{S}{S}: This creature gets +2/+2 until end of turn."
        ));
    }
}
