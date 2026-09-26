package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "99")
public class SinisterStarfish extends Card {

    public SinisterStarfish() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SurveilEffect(1)),
                "{T}: Surveil 1."
        ));
    }
}
