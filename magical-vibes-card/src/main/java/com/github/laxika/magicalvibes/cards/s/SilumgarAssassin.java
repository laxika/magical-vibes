package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "121")
public class SilumgarAssassin extends Card {

    public SilumgarAssassin() {
        addMorph("{2}{B}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new PutCountersOnTurnFaceUpEffect(1));
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtMostPredicate(3),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Target must be a creature with power 3 or less an opponent controls"
        )).addEffect(EffectSlot.ON_TURNED_FACE_UP, new DestroyTargetPermanentEffect());
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtMostSourcePowerPredicate()
                )),
                "creatures with power less than or equal to this creature's power"
        ));
    }
}
