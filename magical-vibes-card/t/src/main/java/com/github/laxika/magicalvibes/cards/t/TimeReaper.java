package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetExiledCardOwnedByDamagedPlayerOnBottomOfOwnersLibraryAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "71")
@CardRegistration(set = "WHO", collectorNumber = "374")
@CardRegistration(set = "WHO", collectorNumber = "676")
@CardRegistration(set = "WHO", collectorNumber = "965")
public class TimeReaper extends Card {

    public TimeReaper() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new PutTargetExiledCardOwnedByDamagedPlayerOnBottomOfOwnersLibraryAndGainLifeEffect(3));
    }
}
