package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "XLN", collectorNumber = "226")
public class RagingSwordtooth extends Card {

    public RagingSwordtooth() {
        // When this creature enters, it deals 1 damage to each other creature.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MassDamageEffect(1, false, false,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
    }
}
