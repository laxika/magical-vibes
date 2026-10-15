package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandOrExileIfUnearthedEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "16")
public class MeticulousExcavation extends Card {

    public MeticulousExcavation() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new ReturnTargetPermanentToHandOrExileIfUnearthedEffect()),
                "{2}{W}: Return target permanent you control to its owner's hand. If it has unearth, "
                        + "instead exile it, then return that card to its owner's hand. Activate only during your turn.",
                TargetFilters.permanentYouControl(),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
