package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WAR", collectorNumber = "214")
public class RoleReversal extends Card {

    public RoleReversal() {
        setMultiTargetConstraint(MultiTargetConstraint.SHARE_PERMANENT_TYPE);

        target(TargetFilters.permanent());
        target(TargetFilters.permanent())
                .addSpellEffect(new ExchangeControlOfTargetPermanentsEffect(
                        new PermanentTruePredicate(), false, false, false, false, false, false, true,
                        null, false));
    }
}
