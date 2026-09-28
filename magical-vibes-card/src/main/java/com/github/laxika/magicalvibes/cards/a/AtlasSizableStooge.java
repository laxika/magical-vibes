package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "566")
public class AtlasSizableStooge extends Card {

    public AtlasSizableStooge() {
        var powerFourOrGreaterCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(4)
        ));
        var qualifyingCreatures = new PermanentCount(powerFourOrGreaterCreature, CountScope.CONTROLLER);

        addEffect(EffectSlot.ON_ATTACK, new GainLifeEffect(qualifyingCreatures));
        addEffect(EffectSlot.ON_BLOCK, new GainLifeEffect(qualifyingCreatures));
    }
}
