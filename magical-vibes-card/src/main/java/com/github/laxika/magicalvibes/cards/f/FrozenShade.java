package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "4ED", collectorNumber = "138")
@CardRegistration(set = "5ED", collectorNumber = "163")
public class FrozenShade extends Card {

    public FrozenShade() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new BoostSelfEffect(1, 1)), "{B}: Frozen Shade gets +1/+1 until end of turn."));
    }
}
