package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "109")
public class NightwingShade extends Card {

    public NightwingShade() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(new BoostSelfEffect(1, 1)), "{1}{B}: Nightwing Shade gets +1/+1 until end of turn."));
    }
}
