package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "139")
@CardRegistration(set = "HA3", collectorNumber = "23")
@CardRegistration(set = "MH2", collectorNumber = "291")
@CardRegistration(set = "C17", collectorNumber = "181")
public class MirarisWake extends Card {

    public MirarisWake() {
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES));
        addTrigger(Triggers.youTapLandForMana(), new AddOneOfEachManaTypeProducedByLandEffect());
    }
}
