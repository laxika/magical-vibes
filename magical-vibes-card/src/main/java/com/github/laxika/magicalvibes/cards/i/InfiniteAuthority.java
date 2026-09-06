package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LEG", collectorNumber = "22")
public class InfiniteAuthority extends Card {

    public InfiniteAuthority() {
        target(TargetFilters.creature());
        DestroyCombatOpponentAtEndOfCombatEffect destroyAndReward =
                new DestroyCombatOpponentAtEndOfCombatEffect(
                        new PermanentToughnessAtMostPredicate(3), false, true);
        addEffect(EffectSlot.ON_BLOCK, destroyAndReward);
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, destroyAndReward, TriggerMode.PER_BLOCKER);
    }
}
