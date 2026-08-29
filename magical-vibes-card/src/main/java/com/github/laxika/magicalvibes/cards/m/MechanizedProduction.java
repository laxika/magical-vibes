package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentsWithSameName;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "38")
public class MechanizedProduction extends Card {

    public MechanizedProduction() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                "Target must be an artifact you control"
        )).addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new CreateTokenCopyOfEnchantedPermanentEffect(),
                ConditionalEffect.unless(
                        new ControlsPermanentsWithSameName(8, new PermanentIsArtifactPredicate()),
                        new WinGameEffect())
        ));
    }
}
