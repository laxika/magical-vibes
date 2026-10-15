package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "99")
public class MoonveilDragon extends Card {

    public MoonveilDragon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostAllOwnCreaturesEffect(1, 0)),
                "{R}: Each creature you control gets +1/+0 until end of turn."
        ));
    }
}
