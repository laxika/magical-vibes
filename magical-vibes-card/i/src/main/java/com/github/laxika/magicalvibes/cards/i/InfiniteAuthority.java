package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "22")
public class InfiniteAuthority extends Card {

    public InfiniteAuthority() {
        target(TargetFilters.creature());
        DestroyCombatOpponentAtEndOfCombatEffect destroyAndReward =
                new DestroyCombatOpponentAtEndOfCombatEffect(
                        new PermanentToughnessAtMostPredicate(3), false, true);
        addTrigger(Triggers.blocks(), destroyAndReward);
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER, destroyAndReward);
    }
}
