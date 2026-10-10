package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "129")
@CardRegistration(set = "MSC", collectorNumber = "300")
@CardRegistration(set = "TDC", collectorNumber = "112")
@CardRegistration(set = "C20", collectorNumber = "25")
public class DismantlingWave extends Card {

    public DismantlingWave() {
        PermanentPredicate artifactOrEnchantmentOpponentControls = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate())),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        target(new PermanentPredicateTargetFilter(
                artifactOrEnchantmentOpponentControls,
                "Targets must be artifacts or enchantments an opponent controls"), 0, 99)
                .addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);

        addCycling("{6}{W}{W}");
        addEffect(EffectSlot.ON_SELF_CYCLED, new DestroyAllPermanentsEffect(new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate()))));
    }
}
