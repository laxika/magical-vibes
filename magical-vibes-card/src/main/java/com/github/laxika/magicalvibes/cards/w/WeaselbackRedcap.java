package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "148")
public class WeaselbackRedcap extends Card {

    public WeaselbackRedcap() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(new BoostSelfEffect(2, 0)),
                "{1}{R}: This creature gets +2/+0 until end of turn."));
    }
}
