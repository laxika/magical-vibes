package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "128")
public class GazeOfPain extends Card {

    public GazeOfPain() {
        // Until end of turn, whenever a creature you control attacks and isn't blocked, you may
        // choose to have it deal damage equal to its power to a target creature. If you do, it
        // assigns no combat damage this turn.
        addSpellEffect(new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(new TriggeredAbility(
                Triggers.creatureYouControlAttacksUnblocked(),
                new MayEffect(SequenceEffect.of(
                        DealDamageToTargetCreatureEffect.byTriggeringPermanent(new TriggeringPermanentPower(true),
                                null),
                        AssignNoCombatDamageEffect.ofTriggeringPermanent()),
                        "have it deal damage equal to its power to a target creature?"))));
    }
}
