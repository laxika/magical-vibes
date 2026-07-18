package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "4ED", collectorNumber = "259")
@CardRegistration(set = "5ED", collectorNumber = "311")
public class LivingArtifact extends Card {

    public LivingArtifact() {
        // Enchant artifact.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Target must be an artifact"
        ))
                // Whenever you're dealt damage, put that many vitality counters on this Aura.
                .addEffect(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE,
                        new PutCountersOnSelfEffect(CounterType.VITALITY, new EventValue()))
                // At the beginning of your upkeep, you may remove a vitality counter from this Aura.
                // If you do, you gain 1 life.
                .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new MayEffect(new RemoveCounterFromSourceAndGainLifeEffect(CounterType.VITALITY, 1),
                                "Remove a vitality counter from Living Artifact to gain 1 life?"));
    }
}
