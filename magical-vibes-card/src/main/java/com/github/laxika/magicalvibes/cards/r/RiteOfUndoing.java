package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "49")
public class RiteOfUndoing extends Card {

    public RiteOfUndoing() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                "First target must be a nonland permanent you control"))
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
