package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "68")
public class LastLaugh extends Card {

    public LastLaugh() {
        addTrigger(Triggers.dies(null, ObjectRelation.ANY),
                new MassDamageEffect(1, true));

        addEffect(EffectSlot.STATE_TRIGGERED, StateTriggerEffect.whenBattlefieldHasAtMost(
                0, new PermanentIsCreaturePredicate(),
                List.of(new SacrificeSelfEffect()),
                "Last Laugh's state-triggered ability"
        ));
    }
}
