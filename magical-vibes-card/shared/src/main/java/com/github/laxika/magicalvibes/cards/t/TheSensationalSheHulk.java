package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class TheSensationalSheHulk extends Card {

    public TheSensationalSheHulk() {
        addStaticEffect(new OpponentsCantCastOrActivateDuringYourTurnEffect(false));
        addTrigger(Triggers.creatureDealtDamage(), new TriggeringPermanentConditionalEffect(
                new PermanentControlledBySourceControllerPredicate(),
                OncePerTurnTriggerEffect.markOnAcceptance(new MayEffect(
                        new DealDamageToAnyTargetEffect(new EventValue()),
                        "Have The Sensational She-Hulk deal that much damage to any target?"))));
    }
}
