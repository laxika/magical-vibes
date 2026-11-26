package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "16")
@CardRegistration(set = "GPT", collectorNumber = "5")
public class GhostWarden extends Card {

    public GhostWarden() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(BoostEffect.target(1, 1)), "{T}: Target creature gets +1/+1 until end of turn."));
    }
}
