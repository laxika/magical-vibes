package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "79")
public class ColosYearling extends Card {

    public ColosYearling() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(1, 0)), "{R}: This creature gets +1/+0 until end of turn."));
    }
}
