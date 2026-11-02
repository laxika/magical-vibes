package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "102")
public class OgreLeadfoot extends Card {

    public OgreLeadfoot() {
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsArtifactPredicate(),
                        new DestroyCreatureBlockingThisEffect()));
    }
}
