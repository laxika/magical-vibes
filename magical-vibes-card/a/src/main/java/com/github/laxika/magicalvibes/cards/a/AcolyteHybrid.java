package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "70")
public class AcolyteHybrid extends Card {

    public AcolyteHybrid() {
        // Whenever this creature attacks, destroy up to one target artifact. If an artifact is
        // destroyed this way, its controller draws a card.
        target(TargetFilters.artifact(), 0, 1).addTrigger(Triggers.attacks(),
                SequenceEffect.of(
                        new DestroyTargetPermanentEffect(),
                        new ConditionalEffect(HappenedThisWay.destroyed(), new DrawCardEffect(1, Subjects.controllerOf(Subjects.target())))));
    }
}
