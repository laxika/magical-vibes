package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "ROE", collectorNumber = "1")
public class AllIsDust extends Card {

    public AllIsDust() {
        addEffect(EffectSlot.SPELL, new SacrificeEachMatchingPermanentEffect(
                new PermanentNotPredicate(new PermanentIsColorlessPredicate())));
    }
}
