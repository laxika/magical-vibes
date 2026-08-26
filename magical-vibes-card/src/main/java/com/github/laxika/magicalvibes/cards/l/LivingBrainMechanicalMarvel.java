package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "167")
public class LivingBrainMechanicalMarvel extends Card {

    public LivingBrainMechanicalMarvel() {
        PermanentPredicate targetArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT))
        ));

        target(new PermanentPredicateTargetFilter(
                targetArtifact,
                "Target must be a non-Equipment artifact you control"
        )).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                new AnimatePermanentsEffect(
                        new Fixed(3), new Fixed(3), List.of(), Set.of(), null, Set.of(CardType.CREATURE),
                        GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN, targetArtifact),
                new UntapPermanentsEffect(TapUntapScope.TARGET)
        ));
    }
}
