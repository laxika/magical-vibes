package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "282")
@CardRegistration(set = "5ED", collectorNumber = "366")
public class DragonEngine extends Card {

    public DragonEngine() {
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new BoostSelfEffect(1, 0)), "{2}: This creature gets +1/+0 until end of turn."));
    }
}
