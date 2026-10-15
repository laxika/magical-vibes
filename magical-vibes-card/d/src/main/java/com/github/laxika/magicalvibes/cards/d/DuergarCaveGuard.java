package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "EVE", collectorNumber = "51")
public class DuergarCaveGuard extends Card {

    public DuergarCaveGuard() {
        addActivatedAbility(new ActivatedAbility(false, "{R/W}", List.of(new BoostSelfEffect(1, 0)), "{R/W}: This creature gets +1/+0 until end of turn."));
    }
}
