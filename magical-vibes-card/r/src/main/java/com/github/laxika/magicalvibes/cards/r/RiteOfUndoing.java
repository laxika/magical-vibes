package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "49")
public class RiteOfUndoing extends Card {

    public RiteOfUndoing() {
        addCastingCost(new DelveCost());

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                "First target must be a nonland permanent you control"))
                .addSpellEffect(ReturnToHandEffect.target());
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addSpellEffect(ReturnToHandEffect.target());
    }
}
