package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AVR", collectorNumber = "154")
public class RushOfBlood extends Card {

    public RushOfBlood() {
        // Target creature gets +X/+0 until end of turn, where X is its power.
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(new TargetPower(), new Fixed(0)));
    }
}
