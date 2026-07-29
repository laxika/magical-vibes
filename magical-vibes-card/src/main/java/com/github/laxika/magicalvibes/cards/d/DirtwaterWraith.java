package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "117")
public class DirtwaterWraith extends Card {

    public DirtwaterWraith() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new BoostSelfEffect(1, 0)), "{B}: Dirtwater Wraith gets +1/+0 until end of turn."));
    }
}
