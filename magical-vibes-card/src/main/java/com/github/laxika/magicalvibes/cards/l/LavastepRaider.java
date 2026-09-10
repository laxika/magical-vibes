package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "147")
public class LavastepRaider extends Card {

    public LavastepRaider() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{R}", List.of(new BoostSelfEffect(2, 0)),
                "{2}{R}: This creature gets +2/+0 until end of turn."));
    }
}
