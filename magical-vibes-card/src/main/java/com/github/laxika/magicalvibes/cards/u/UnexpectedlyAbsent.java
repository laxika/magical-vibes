package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EMA", collectorNumber = "33")
@CardRegistration(set = "C13", collectorNumber = "25")
public class UnexpectedlyAbsent extends Card {

    public UnexpectedlyAbsent() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new PutTargetPermanentIntoLibraryNFromTopEffect(new XValue()));
    }
}
