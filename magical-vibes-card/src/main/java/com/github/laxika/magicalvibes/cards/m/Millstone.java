package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "334")
public class Millstone extends Card {

    public Millstone() {
        addActivatedAbility(new ActivatedAbility(true, "{2}", List.of(new MillTargetPlayerEffect(2)), true, "{2}, {T}: Target player mills two cards."));
    }
}
