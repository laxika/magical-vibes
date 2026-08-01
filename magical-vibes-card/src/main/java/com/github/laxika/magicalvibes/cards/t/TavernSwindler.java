package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "79")
public class TavernSwindler extends Card {

    public TavernSwindler() {
        // {T}, Pay 3 life: Flip a coin. If you win the flip, you gain 6 life.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new PayLifeCost(3),
                        new FlipCoinWinEffect(new GainLifeEffect(6))),
                "{T}, Pay 3 life: Flip a coin. If you win the flip, you gain 6 life."));
    }
}
