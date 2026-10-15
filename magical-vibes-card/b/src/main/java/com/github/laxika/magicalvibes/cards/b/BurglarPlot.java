package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class BurglarPlot extends Card {

    public BurglarPlot() {
        PermanentNotPredicate nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());
        setMultiTargetConstraint(MultiTargetConstraint.SHARE_CARD_TYPE);
        target(TargetFilters.nonlandPermanent());
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL,
                        ExchangeControlOfTargetPermanentsEffect.withSharedCardType(nonland));
    }
}
