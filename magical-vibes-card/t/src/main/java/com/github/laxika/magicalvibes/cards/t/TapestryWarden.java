package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UseToughnessForStationEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessGreaterThanPowerPredicate;

@CardRegistration(set = "EOE", collectorNumber = "209")
public class TapestryWarden extends Card {

    public TapestryWarden() {
        addStaticEffect(new AssignCombatDamageWithToughnessEffect(
                Subjects.creaturesYouControl(), new PermanentToughnessGreaterThanPowerPredicate()));
        addStaticEffect(new UseToughnessForStationEffect());
    }
}
