package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "58")
@CardRegistration(set = "TPR", collectorNumber = "103")
public class DungeonShade extends Card {

    public DungeonShade() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(BoostEffect.self(1, 1)), "{B}: This creature gets +1/+1 until end of turn."));
    }
}
