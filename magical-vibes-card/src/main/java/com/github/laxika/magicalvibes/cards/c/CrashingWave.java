package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseXValueCost;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "47")
public class CrashingWave extends Card {

    public CrashingWave() {
        addEffect(EffectSlot.SPELL, new ChooseXValueCost(0, 100));
        addEffect(EffectSlot.SPELL, WaterbendCost.x());
        targetX(TargetFilters.creature(), 100)
                .addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.SPELL, new DistributeCountersAmongControlledCreaturesEffect(
                CounterType.STUN, 3,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsTappedPredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())))));
    }
}
