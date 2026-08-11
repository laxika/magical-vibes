package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;

@CardRegistration(set = "ECL", collectorNumber = "78")
public class TemporalCleansing extends Card {

    public TemporalCleansing() {
        addEffect(EffectSlot.SPELL, new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(1));
    }
}
