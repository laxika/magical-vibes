package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerAttackedBySourceLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "2")
public class AngelOfDestiny extends Card {

    public AngelOfDestiny() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCreaturePredicate(),
                        SequenceEffect.of(
                                new GainLifeEffect(new EventValue()),
                                new GainLifeEffect(new EventValue(), GainLifeRecipient.TRIGGERING_PLAYER))));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerLifeAtLeast(15, true)),
                new EachPlayerAttackedBySourceLosesGameEffect());
    }
}
