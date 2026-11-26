package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "42")
@CardRegistration(set = "M20", collectorNumber = "66")
public class MetropolisSprite extends Card {

    public MetropolisSprite() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(BoostEffect.self(1, -1)),
                "{U}: Metropolis Sprite gets +1/-1 until end of turn."));
    }
}
