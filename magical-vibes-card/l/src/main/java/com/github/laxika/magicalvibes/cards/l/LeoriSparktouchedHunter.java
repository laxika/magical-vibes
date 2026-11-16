package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyAbilitiesOfChosenPlaneswalkerTypeThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "709")
@CardRegistration(set = "CMM", collectorNumber = "774")
public class LeoriSparktouchedHunter extends Card {

    public LeoriSparktouchedHunter() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new CopyAbilitiesOfChosenPlaneswalkerTypeThisTurnEffect());
    }
}
