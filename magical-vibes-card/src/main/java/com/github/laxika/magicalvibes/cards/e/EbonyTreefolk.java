package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "97")
public class EbonyTreefolk extends Card {

    public EbonyTreefolk() {
        addActivatedAbility(new ActivatedAbility(false, "{B}{G}", List.of(new BoostSelfEffect(1, 1)),
                "{B}{G}: This creature gets +1/+1 until end of turn."));
    }
}
