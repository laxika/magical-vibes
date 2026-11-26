package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "1")
public class AngelfireCrusader extends Card {

    public AngelfireCrusader() {
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(BoostEffect.self(1, 0)),
                "{R}: This creature gets +1/+0 until end of turn."));
    }
}
