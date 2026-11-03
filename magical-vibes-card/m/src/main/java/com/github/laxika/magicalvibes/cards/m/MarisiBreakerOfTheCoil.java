package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesTargetPlayerControlsUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsDuringCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C19", collectorNumber = "46")
public class MarisiBreakerOfTheCoil extends Card {

    public MarisiBreakerOfTheCoil() {
        addStaticEffect(new OpponentsCantCastSpellsDuringCombatEffect());
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new GoadCreaturesTargetPlayerControlsUntilNextTurnEffect());
    }
}
