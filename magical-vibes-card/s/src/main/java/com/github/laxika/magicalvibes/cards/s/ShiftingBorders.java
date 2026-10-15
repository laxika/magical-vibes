package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "56")
public class ShiftingBorders extends Card {

    public ShiftingBorders() {
        target(new PermanentPredicateTargetFilter(new PermanentIsLandPredicate(), "First target must be a land"));

        target(new PermanentPredicateTargetFilter(new PermanentIsLandPredicate(), "Second target must be a land"))
                .addEffect(EffectSlot.SPELL, new ExchangeControlOfTargetPermanentsEffect(
                        new PermanentIsLandPredicate(), false));

        addEffect(EffectSlot.STATIC, new SpliceEffect(CardSubtype.ARCANE, "{3}{U}"));
    }
}
