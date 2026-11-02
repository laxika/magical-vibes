package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "216")
@CardRegistration(set = "DDU", collectorNumber = "51")
public class ReclusiveArtificer extends Card {

    public ReclusiveArtificer() {
        // When this creature enters, you may have it deal damage to target creature equal to the
        // number of artifacts you control. The target is chosen as the trigger goes on the stack;
        // the may only decides whether the damage happens, and the amount is counted on resolution.
        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new DealDamageToTargetCreatureEffect(
                        new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER)),
                        "Deal damage equal to the number of artifacts you control to target creature?"));
    }
}
