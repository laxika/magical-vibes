package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "82")
public class WallOfBlood extends Card {

    public WallOfBlood() {
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(1), new BoostSelfEffect(1, 1)),
                "Pay 1 life: This creature gets +1/+1 until end of turn."));
    }
}
