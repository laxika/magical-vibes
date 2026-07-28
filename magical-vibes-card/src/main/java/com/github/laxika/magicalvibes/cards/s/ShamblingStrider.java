package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "263")
public class ShamblingStrider extends Card {

    public ShamblingStrider() {
        addActivatedAbility(new ActivatedAbility(false, "{R}{G}", List.of(new BoostSelfEffect(1, -1)), "{R}{G}: This creature gets +1/-1 until end of turn."));
    }
}
