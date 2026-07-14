package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "EVE", collectorNumber = "94")
public class RestlessApparition extends Card {

    public RestlessApparition() {
        addActivatedAbility(new ActivatedAbility(false, "{W/B}{W/B}{W/B}", List.of(new BoostSelfEffect(3, 3)), "{W/B}{W/B}{W/B}: This creature gets +3/+3 until end of turn."));
    }
}
