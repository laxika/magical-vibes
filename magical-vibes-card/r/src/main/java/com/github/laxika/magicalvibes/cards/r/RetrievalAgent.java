package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "60")
public class RetrievalAgent extends Card {

    public RetrievalAgent() {
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(BoostEffect.self(1, -1)),
                "{2}: This creature gets +1/-1 until end of turn."));
    }
}
