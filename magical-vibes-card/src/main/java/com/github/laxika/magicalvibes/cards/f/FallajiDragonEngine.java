package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "159")
public class FallajiDragonEngine extends Card {

    public FallajiDragonEngine() {
        addPrototype("{2}{R}", CardColor.RED, 1, 3);
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new BoostSelfEffect(1, 0)),
                "{2}: This creature gets +1/+0 until end of turn."));
    }
}
