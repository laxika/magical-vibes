package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "20")
public class MoonwingMoth extends Card {

    public MoonwingMoth() {
        addActivatedAbility(new ActivatedAbility(false, "{W}", List.of(new BoostSelfEffect(0, 1)),
                "{W}: Moonwing Moth gets +0/+1 until end of turn."));
    }
}
