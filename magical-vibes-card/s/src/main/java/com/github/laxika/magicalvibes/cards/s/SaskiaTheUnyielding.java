package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "55")
@CardRegistration(set = "C16", collectorNumber = "41")
public class SaskiaTheUnyielding extends Card {

    public SaskiaTheUnyielding() {
        addStaticEffect(new ChoosePlayerOnEnterEffect());
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentIsCreaturePredicate()),
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.CHOSEN_PLAYER)
                        .dealtByEventDamageSource());
    }
}
