package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "14")
public class ManaclesOfDecay extends Card {

    public ManaclesOfDecay() {
        target(TargetFilters.creature())
                .addStaticEffect(new EnchantedCreatureCantAttackOrBlockEffect(true, false));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(BoostEffect.of(Subjects.attached(), -1, -1)),
                "{B}: Enchanted creature gets -1/-1 until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new CantBlockThisTurnEffect(Subjects.attached())),
                "{R}: Enchanted creature can't block this turn."
        ));
    }
}
