package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "51")
public class ShiftingLoyalties extends Card {

    public ShiftingLoyalties() {
        setMultiTargetConstraint(MultiTargetConstraint.SHARE_CARD_TYPE);
        target(TargetFilters.permanent());
        target(TargetFilters.permanent())
                .addSpellEffect(ExchangeControlOfTargetPermanentsEffect.withSharedCardType(
                                new PermanentTruePredicate()));
    }
}
