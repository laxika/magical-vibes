package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "4ED", collectorNumber = "138")
@CardRegistration(set = "5ED", collectorNumber = "163")
@CardRegistration(set = "SUM", collectorNumber = "112")
@CardRegistration(set = "3ED", collectorNumber = "112")
@CardRegistration(set = "2ED", collectorNumber = "110")
@CardRegistration(set = "LEA", collectorNumber = "109")
@CardRegistration(set = "LEB", collectorNumber = "110")
public class FrozenShade extends Card {

    public FrozenShade() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(BoostEffect.self(1, 1)), "{B}: Frozen Shade gets +1/+1 until end of turn."));
    }
}
