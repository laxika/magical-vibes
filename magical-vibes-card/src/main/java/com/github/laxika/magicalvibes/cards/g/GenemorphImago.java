package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EOE", collectorNumber = "217")
public class GenemorphImago extends Card {

    public GenemorphImago() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                        new SetBasePowerToughnessEffect(3, 3))
                .addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                        ConditionalEffect.unless(
                                new ControlsPermanentCount(6, new PermanentIsLandPredicate()),
                                new SetBasePowerToughnessEffect(6, 6)));
    }
}
