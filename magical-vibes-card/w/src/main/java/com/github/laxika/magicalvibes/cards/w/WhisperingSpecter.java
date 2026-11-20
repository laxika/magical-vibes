package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerPoisonCounters;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "77")
public class WhisperingSpecter extends Card {

    public WhisperingSpecter() {
        // "That player" is the damaged player, whom the trigger records as the entry's player.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(SequenceEffect.of(
                        new SacrificeSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.sacrificed(),
                                new DiscardEffect(new TargetPlayerPoisonCounters(), Subjects.triggeringPlayer()))),
                        "You may sacrifice it. If you do, that player discards a card for each poison counter they have."));
    }
}
