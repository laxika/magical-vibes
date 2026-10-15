package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "115")
public class FoxfireOak extends Card {

    public FoxfireOak() {
        addActivatedAbility(new ActivatedAbility(false, "{R/G}{R/G}{R/G}", List.of(new BoostSelfEffect(3, 0)), "{R/G}{R/G}{R/G}: Foxfire Oak gets +3/+0 until end of turn."));
    }
}
