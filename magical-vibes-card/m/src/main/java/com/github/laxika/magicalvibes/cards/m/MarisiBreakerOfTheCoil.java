package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C19", collectorNumber = "46")
public class MarisiBreakerOfTheCoil extends Card {

    public MarisiBreakerOfTheCoil() {
        addStaticEffect(OpponentsCantCastOrActivateDuringYourTurnEffect.duringCombat(Subjects.eachOpponent()));
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                GoadTargetCreatureUntilNextTurnEffect.creaturesControlledBy(Subjects.triggeringPlayer()));
    }
}
