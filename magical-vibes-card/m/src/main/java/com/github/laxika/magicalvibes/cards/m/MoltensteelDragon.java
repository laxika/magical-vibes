package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "88")
public class MoltensteelDragon extends Card {

    public MoltensteelDragon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R/P}",
                List.of(new BoostSelfEffect(1, 0)),
                "{R/P}: Moltensteel Dragon gets +1/+0 until end of turn."
        ));
    }
}
