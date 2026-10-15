package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "138")
public class MyrQuadropod extends Card {

    public MyrQuadropod() {
        addActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new SwitchPowerToughnessEffect(true)),
                "{3}: Switch this creature's power and toughness until end of turn."));
    }
}
