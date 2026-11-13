package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "157")
public class SprintingWarbrute extends Card {

    public SprintingWarbrute() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{R}"))));
        addStaticEffect(new MustAttackEffect());
    }
}
