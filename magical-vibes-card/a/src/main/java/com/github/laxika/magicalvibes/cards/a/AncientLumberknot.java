package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessGreaterThanPowerPredicate;

@CardRegistration(set = "VOW", collectorNumber = "230")
@CardRegistration(set = "DBL", collectorNumber = "497")
public class AncientLumberknot extends Card {

    public AncientLumberknot() {
        addStaticEffect(new AssignCombatDamageWithToughnessEffect(
                Subjects.creaturesYouControl(),
                new PermanentToughnessGreaterThanPowerPredicate()));
    }
}
