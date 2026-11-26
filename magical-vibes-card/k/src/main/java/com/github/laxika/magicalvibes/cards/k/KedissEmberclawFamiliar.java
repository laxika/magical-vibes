package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "2364")
public class KedissEmberclawFamiliar extends Card {

    public KedissEmberclawFamiliar() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentIsCommanderPredicate()),
                DealDamageEffect.to(Subjects.except(Subjects.eachOpponent(), Subjects.triggeringPlayer()),
                        new EventValue()).byEventDamageSource());
    }
}
