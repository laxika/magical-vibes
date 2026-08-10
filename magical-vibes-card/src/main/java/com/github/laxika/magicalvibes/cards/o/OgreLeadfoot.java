package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MRD", collectorNumber = "102")
public class OgreLeadfoot extends Card {

    public OgreLeadfoot() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsArtifactPredicate(),
                        new DestroyCreatureBlockingThisEffect()),
                TriggerMode.PER_BLOCKER);
    }
}
