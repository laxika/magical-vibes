package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "140")
public class ViashivanDragon extends Card {

    public ViashivanDragon() {
        // {R}: This creature gets +1/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(1, 0)),
                "{R}: This creature gets +1/+0 until end of turn."));

        // {G}: This creature gets +0/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(new BoostSelfEffect(0, 1)),
                "{G}: This creature gets +0/+1 until end of turn."));
    }
}
