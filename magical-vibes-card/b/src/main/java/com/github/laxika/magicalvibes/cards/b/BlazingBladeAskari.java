package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "146")
public class BlazingBladeAskari extends Card {

    public BlazingBladeAskari() {
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new BecomeColorlessUntilEndOfTurnEffect(false)),
                "{2}: This creature becomes colorless until end of turn."));
    }
}
