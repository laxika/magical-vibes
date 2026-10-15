package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerGreaterThanActivePlayerHandSizePredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "304")
public class NoeticScales extends Card {

    public NoeticScales() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, ReturnToHandEffect.allPermanentsMatching(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledByActivePlayerPredicate(),
                        new PermanentPowerGreaterThanActivePlayerHandSizePredicate()))));
    }
}
