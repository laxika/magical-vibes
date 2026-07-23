package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "104")
public class ThunderWall extends Card {

    public ThunderWall() {
        // {U}: This creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new BoostSelfEffect(1, 1)),
                "{U}: This creature gets +1/+1 until end of turn."));
    }
}
