package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "SUM", collectorNumber = "156")
public class GraniteGargoyle extends Card {

    public GraniteGargoyle() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostSelfEffect(0, 1)),
                "{R}: Granite Gargoyle gets +0/+1 until end of turn."
        ));
    }
}
