package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlipCoinForEachMatchingPermanentDestroyOnLossEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "199")
public class RakdosTheShowstopper extends Card {

    public RakdosTheShowstopper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new FlipCoinForEachMatchingPermanentDestroyOnLossEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(
                                        new PermanentHasAnySubtypePredicate(
                                                Set.of(CardSubtype.DEMON, CardSubtype.DEVIL, CardSubtype.IMP)))))));
    }
}
