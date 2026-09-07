package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepAndDamageForEachStillExiledEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "132")
public class DragonhawkFatesTempest extends Card {

    public DragonhawkFatesTempest() {
        var powerFourCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(4)
        ));
        var effect = new ExileTopCardsMayPlayUntilNextEndStepAndDamageForEachStillExiledEffect(
                new PermanentCount(powerFourCreatures, CountScope.CONTROLLER), 2);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, effect);
        addEffect(EffectSlot.ON_ATTACK, effect);
    }
}
