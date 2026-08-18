package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "10")
public class GhostLitRedeemer extends Card {

    public GhostLitRedeemer() {
        addActivatedAbility(new ActivatedAbility(true, "{W}",
                List.of(new GainLifeEffect(2)),
                "{W}, {T}: You gain 2 life."));

        addHandActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(new GainLifeEffect(4)),
                "Channel — {1}{W}, Discard this card: You gain 4 life."));
    }
}
