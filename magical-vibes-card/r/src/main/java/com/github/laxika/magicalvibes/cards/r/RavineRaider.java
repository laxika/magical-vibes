package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "106")
public class RavineRaider extends Card {

    public RavineRaider() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(BoostEffect.self(1, 1)),
                "{1}{B}: Ravine Raider gets +1/+1 until end of turn."));
    }
}
