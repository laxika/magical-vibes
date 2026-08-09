package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.UntapChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "38")
public class RisingWaters extends Card {

    public RisingWaters() {
        // Lands don't untap during their controllers' untap steps.
        addEffect(EffectSlot.STATIC, new MatchingPermanentsDoesntUntapEffect(new PermanentIsLandPredicate()));

        // At the beginning of each player's upkeep, that player untaps a land they control.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new UntapChosenPermanentEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentControlledByActivePlayerPredicate())),
                true));
    }
}
