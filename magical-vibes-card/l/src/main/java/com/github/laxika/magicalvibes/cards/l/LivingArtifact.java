package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CounterRemovalSubject;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "4ED", collectorNumber = "259")
@CardRegistration(set = "5ED", collectorNumber = "311")
@CardRegistration(set = "SUM", collectorNumber = "209")
@CardRegistration(set = "LEB", collectorNumber = "209")
@CardRegistration(set = "3ED", collectorNumber = "209")
@CardRegistration(set = "2ED", collectorNumber = "209")
@CardRegistration(set = "LEA", collectorNumber = "208")
public class LivingArtifact extends Card {

    public LivingArtifact() {
        // Enchant artifact.
        target(TargetFilters.artifact())
                // Whenever you're dealt damage, put that many vitality counters on this Aura.
                .addTrigger(Triggers.youAreDealtDamage(),
                        new PutCountersOnSelfEffect(CounterType.VITALITY, new EventValue()))
                // At the beginning of your upkeep, you may remove a vitality counter from this Aura.
                // If you do, you gain 1 life.
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                        new MayEffect(new RemoveCounterAndGainLifeEffect(CounterType.VITALITY, 1, CounterRemovalSubject.SOURCE),
                                "Remove a vitality counter from Living Artifact to gain 1 life?"));
    }
}
